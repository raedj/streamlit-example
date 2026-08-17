Attribute VB_Name = "modOffBalanceRulesEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modOffBalanceRulesEngine
' 3-year eligibility screening and 6-year mandatory escalation, both
' anchored to Account.DefaultDate and both driven by thresholds in
' tbl_RegulatoryRules (RuleCode = OFFBALANCE_3YEAR / OFFBALANCE_6YEAR) —
' never a hard-coded "3" or "6" in this module.
' ============================================================================

' ----------------------------------------------------------------------
' RunOffBalanceScreening
' Call once per on-balance account during month-end close.
' ----------------------------------------------------------------------
Public Sub RunOffBalanceScreening(ByVal AccountID As Long, ByVal AsOfDate As Date)
    Dim defaultDate As Variant
    Dim ageYears As Double
    Dim threshold3yr As Double, threshold6yr As Double
    Dim offBalanceStatus As String

    offBalanceStatus = Nz(DLookup("OffBalanceStatus", "tbl_Account", "AccountID = " & AccountID), "On-Balance")
    If offBalanceStatus <> "On-Balance" Then Exit Sub   ' already off-balance, nothing to screen

    defaultDate = DLookup("DefaultDate", "tbl_Account", "AccountID = " & AccountID)
    If IsNull(defaultDate) Then Exit Sub   ' flagged separately by modDataQualityEngine

    ageYears = DateDiff("d", CDate(defaultDate), AsOfDate) / 365.25

    threshold3yr = GetActiveRuleThreshold("OFFBALANCE_3YEAR", AsOfDate)
    threshold6yr = GetActiveRuleThreshold("OFFBALANCE_6YEAR", AsOfDate)

    If ageYears >= threshold3yr Then
        EvaluateThreeYearEligibility AccountID, AsOfDate
    End If

    If ageYears >= threshold6yr Then
        EnsureSixYearException AccountID, AsOfDate
    End If
End Sub

Private Function GetActiveRuleThreshold(ByVal RuleCode As String, ByVal AsOfDate As Date) As Double
    Dim v As Variant
    v = DLookup("ThresholdValue", "tbl_RegulatoryRules", _
        "RuleCode = '" & RuleCode & "' AND Active = True AND EffectiveDate <= #" & Format(AsOfDate, "yyyy-mm-dd") & _
        "# AND (ExpiryDate IS NULL OR ExpiryDate > #" & Format(AsOfDate, "yyyy-mm-dd") & "#)")
    If IsNull(v) Then
        RaiseEngineError "modOffBalanceRulesEngine.GetActiveRuleThreshold", 5301, _
            "No active tbl_RegulatoryRules row for '" & RuleCode & "' as of " & Format(AsOfDate, "yyyy-mm-dd") & "."
    End If
    GetActiveRuleThreshold = v
End Function

' ----------------------------------------------------------------------
' EvaluateThreeYearEligibility
' Advisory only — writes a KPI/flag row for the regulatory dashboard, does
' NOT execute an off-balance transaction. Execution always goes through
' the manual Off-Balance form -> modApprovalWorkflowEngine.
' ----------------------------------------------------------------------
Private Sub EvaluateThreeYearEligibility(ByVal AccountID As Long, ByVal AsOfDate As Date)
    Dim provisionCoverageOK As Boolean, siCoverageOK As Boolean
    Dim legalReviewOK As Boolean, collateralReviewOK As Boolean
    Dim minCoverage As Double

    minCoverage = GetActiveRuleThreshold("PROVISION_COVERAGE_MIN", AsOfDate)

    Dim exposure As Currency, provision As Currency
    exposure = Nz(DLookup("CurrentExposure", "tbl_Account", "AccountID = " & AccountID), 0)
    provision = Nz(DLookup("ProvisionBalance", "tbl_Account", "AccountID = " & AccountID), 0)

    provisionCoverageOK = (exposure = 0) Or ((provision / exposure) >= minCoverage)
    siCoverageOK = True ' per bank's SI coverage rule; wire to tbl_RegulatoryRules('SI_COVERAGE_MIN') the same way
    legalReviewOK = (DCount("*", "tbl_LegalCase", "AccountID = " & AccountID & " AND CaseStatus IN ('Judgment','Execution')") > 0) _
                    Or (DLookup("LegalStatus", "tbl_Account", "AccountID = " & AccountID) = "None")
    collateralReviewOK = (DCount("*", "tbl_CollateralValuation", "CollateralID IN (SELECT CollateralID FROM tbl_Collateral WHERE AccountID = " & AccountID & ") AND ValuationDate >= #" & Format(DateAdd("yyyy", -1, AsOfDate), "yyyy-mm-dd") & "#") > 0)

    Dim eligible As Boolean
    eligible = provisionCoverageOK And siCoverageOK And legalReviewOK And collateralReviewOK

    CurrentDb.Execute "INSERT INTO tbl_KPIResult (KPIPeriod, KPICode, Scope, ScopeID, KPIValue) VALUES (" & _
        "#" & Format(AsOfDate, "yyyy-mm-dd") & "#, 'OFFBALANCE_3YR_ELIGIBLE', 'Account', " & AccountID & ", " & _
        IIf(eligible, 1, 0) & ")"
End Sub

' ----------------------------------------------------------------------
' EnsureSixYearException
' Mandatory: an account past the 6-year threshold that is still on-balance
' MUST carry an open (or explicitly cleared) tbl_RegulatoryException row.
' This does not resolve or approve anything — it only guarantees the
' exception exists so the account cannot be invisible to the regulatory
' dashboard.
' ----------------------------------------------------------------------
Private Sub EnsureSixYearException(ByVal AccountID As Long, ByVal AsOfDate As Date)
    Dim ruleID As Long
    ruleID = Nz(DLookup("RegulatoryRuleID", "tbl_RegulatoryRules", _
        "RuleCode = 'OFFBALANCE_6YEAR' AND Active = True"), 0)

    Dim openExceptionExists As Boolean
    openExceptionExists = DCount("*", "tbl_RegulatoryException", _
        "AccountID = " & AccountID & " AND RuleID = " & ruleID & " AND Status <> 'Cleared'") > 0

    If openExceptionExists Then Exit Sub

    CurrentDb.Execute "INSERT INTO tbl_RegulatoryException " & _
        "(AccountID, RuleID, TriggerDate, ReasonCode, Justification, Status) VALUES (" & _
        AccountID & ", " & ruleID & ", #" & Format(AsOfDate, "yyyy-mm-dd") & "#, " & _
        "'PENDING_JUSTIFICATION', 'Auto-created by month-end close: account exceeds the 6-year " & _
        "off-balance threshold and remains on-balance. Requires responsible manager, justification, " & _
        "expected recovery, and approval.', 'Open')"

    LogAuditEvent ActionType:="Insert", TableName:="tbl_RegulatoryException", RecordID:=0, AccountID:=AccountID, _
        Reason:="Mandatory 6-year escalation auto-created by month-end close.", _
        SourceContext:="modOffBalanceRulesEngine.EnsureSixYearException"

    WriteLog "modOffBalanceRulesEngine", "6-year exception created for Account " & AccountID, "WARN"
End Sub

' ----------------------------------------------------------------------
' ExceptionAgingBucket
' 0-30 / 31-90 / 91-180 / 181-365 / >365 days since NextReviewDate was due
' (falls back to TriggerDate if the exception has never been reviewed).
' ----------------------------------------------------------------------
Public Function ExceptionAgingBucket(ByVal RegulatoryExceptionID As Long, ByVal AsOfDate As Date) As String
    Dim dueDate As Variant
    Dim daysOverdue As Long

    dueDate = DLookup("NextReviewDate", "tbl_RegulatoryException", "RegulatoryExceptionID = " & RegulatoryExceptionID)
    If IsNull(dueDate) Then
        dueDate = DLookup("TriggerDate", "tbl_RegulatoryException", "RegulatoryExceptionID = " & RegulatoryExceptionID)
    End If

    daysOverdue = DateDiff("d", CDate(dueDate), AsOfDate)
    If daysOverdue < 0 Then daysOverdue = 0

    Select Case daysOverdue
        Case 0 To 30:    ExceptionAgingBucket = "0-30"
        Case 31 To 90:   ExceptionAgingBucket = "31-90"
        Case 91 To 180:  ExceptionAgingBucket = "91-180"
        Case 181 To 365: ExceptionAgingBucket = "181-365"
        Case Else:       ExceptionAgingBucket = ">365"
    End Select
End Function
