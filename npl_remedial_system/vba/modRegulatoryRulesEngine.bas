Attribute VB_Name = "modRegulatoryRulesEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modRegulatoryRulesEngine
' Single point of truth for every configurable regulatory threshold and
' system parameter. No other module should query tbl_RegulatoryRules or
' tbl_SystemParameter directly except through the functions here (or,
' where a fast bulk lookup is needed, modUtilities.GetSystemParameter) —
' centralizing it means a threshold change is a data change, auditable
' through tbl_AuditTrail, never a code deployment.
' ============================================================================

' ----------------------------------------------------------------------
' GetActiveRule
' Returns the full tbl_RegulatoryRules row (as a Recordset the caller must
' close) effective as of AsOfDate for a given RuleCode. Raising rather
' than defaulting on a missing rule is deliberate: a silently-defaulted
' regulatory threshold is exactly the failure mode this module exists to
' prevent.
' ----------------------------------------------------------------------
Public Function GetActiveRule(ByVal RuleCode As String, Optional ByVal AsOfDate As Variant) As DAO.Recordset
    Dim db As DAO.Database
    Dim rs As DAO.Recordset
    Dim effDate As Date

    If IsMissing(AsOfDate) Or IsNull(AsOfDate) Then
        effDate = Date
    Else
        effDate = CDate(AsOfDate)
    End If

    Set db = CurrentDb
    Set rs = db.OpenRecordset( _
        "SELECT * FROM tbl_RegulatoryRules WHERE RuleCode = '" & RuleCode & "' AND Active = True " & _
        "AND EffectiveDate <= #" & Format(effDate, "yyyy-mm-dd") & "# " & _
        "AND (ExpiryDate IS NULL OR ExpiryDate > #" & Format(effDate, "yyyy-mm-dd") & "#) " & _
        "ORDER BY EffectiveDate DESC")

    If rs.EOF Then
        RaiseEngineError "modRegulatoryRulesEngine.GetActiveRule", 5601, _
            "No active regulatory rule '" & RuleCode & "' effective " & Format(effDate, "yyyy-mm-dd") & "."
    End If

    Set GetActiveRule = rs
End Function

' ----------------------------------------------------------------------
' GetActiveRuleThreshold
' Convenience wrapper returning just ThresholdValue — what most engine
' code actually needs (see modOffBalanceRulesEngine, modApprovalWorkflowEngine).
' ----------------------------------------------------------------------
Public Function GetActiveRuleThreshold(ByVal RuleCode As String, Optional ByVal AsOfDate As Variant) As Double
    Dim rs As DAO.Recordset
    Set rs = GetActiveRule(RuleCode, AsOfDate)
    GetActiveRuleThreshold = rs!ThresholdValue
    rs.Close
    Set rs = Nothing
End Function

' ----------------------------------------------------------------------
' ListRulesExpiringSoon
' Supports an Admin-facing alert: rules whose ExpiryDate falls within the
' next WarningDays, so a threshold doesn't silently lapse back to "no
' active rule" and start raising GetActiveRule errors mid-month-end.
' ----------------------------------------------------------------------
Public Function ListRulesExpiringSoon(ByVal WarningDays As Integer) As DAO.Recordset
    Dim db As DAO.Database
    Set db = CurrentDb
    Set ListRulesExpiringSoon = db.OpenRecordset( _
        "SELECT RuleCode, RuleName, ExpiryDate FROM tbl_RegulatoryRules " & _
        "WHERE Active = True AND ExpiryDate IS NOT NULL " & _
        "AND ExpiryDate BETWEEN Date() AND DateAdd('d', " & WarningDays & ", Date()) " & _
        "ORDER BY ExpiryDate")
End Function

' ----------------------------------------------------------------------
' UpdateRuleThreshold
' The ONLY sanctioned way to change a threshold: closes out the current
' rule row (sets ExpiryDate) and inserts a new one, preserving history —
' never an in-place UPDATE of ThresholdValue on an existing row, so past
' snapshots/screenings stay explainable by "the rule in force at the
' time," not silently reinterpreted under today's threshold. Requires
' Admin role (tbl_UserRole.CanConfigure) — checked by the calling form,
' logged here regardless.
' ----------------------------------------------------------------------
Public Sub UpdateRuleThreshold(ByVal RuleCode As String, ByVal NewThresholdValue As Double, _
                                ByVal NewEffectiveDate As Date, ByVal Reason As String)
    Dim db As DAO.Database
    Dim currentRuleID As Long
    Dim templateRow As DAO.Recordset

    Set db = CurrentDb
    Set templateRow = GetActiveRule(RuleCode, NewEffectiveDate - 1)
    currentRuleID = templateRow!RegulatoryRuleID

    db.Execute "UPDATE tbl_RegulatoryRules SET ExpiryDate = #" & Format(NewEffectiveDate, "yyyy-mm-dd") & _
        "# WHERE RegulatoryRuleID = " & currentRuleID

    db.Execute "INSERT INTO tbl_RegulatoryRules " & _
        "(RuleCode, RuleName, RuleType, ThresholdValue, ThresholdUnit, EffectiveDate, ApplicablePortfolio, " & _
        "CalculationMethod, RequiredAction, ApprovalLevel, RegulatorySource, Active) " & _
        "SELECT RuleCode, RuleName, RuleType, " & NewThresholdValue & ", ThresholdUnit, #" & _
        Format(NewEffectiveDate, "yyyy-mm-dd") & "#, ApplicablePortfolio, CalculationMethod, RequiredAction, " & _
        "ApprovalLevel, RegulatorySource, True FROM tbl_RegulatoryRules WHERE RegulatoryRuleID = " & currentRuleID

    LogAuditEvent ActionType:="Config", TableName:="tbl_RegulatoryRules", RecordID:=currentRuleID, _
        FieldName:="ThresholdValue", OldValue:=templateRow!ThresholdValue, NewValue:=NewThresholdValue, _
        Reason:=Reason, SourceContext:="modRegulatoryRulesEngine.UpdateRuleThreshold"

    templateRow.Close
    Set templateRow = Nothing
    Set db = Nothing
End Sub
