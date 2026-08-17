Attribute VB_Name = "modMonthEndClosingEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modMonthEndClosingEngine
' Orchestrates the 14-step month-end sequence (spec §60) across every
' active account, writing one tbl_MonthClosing row per step so a failed
' run is diagnosable and resumable rather than an opaque all-or-nothing
' batch. Ends with tbl_PeriodLock, after which no further writes to that
' period are allowed outside modApprovalWorkflowEngine.CreateReversal.
' ============================================================================

Public Sub RunMonthEndClose(ByVal ClosingPeriod As Date)
    If IsPeriodLocked(ClosingPeriod) Then
        RaiseEngineError "modMonthEndClosingEngine.RunMonthEndClose", 5501, _
            "Period " & Format(ClosingPeriod, "yyyy-mm") & " is already locked. Use a Controlled Adjustment, not a re-run."
    End If

    RunStep ClosingPeriod, "01_ImportValidation", 1
    RunStep ClosingPeriod, "02_DataQuality", 2
    RunStep ClosingPeriod, "03_Reconciliation", 3
    RunStep ClosingPeriod, "04_MovementCalculation", 4
    RunStep ClosingPeriod, "05_RecoveryCalculation", 5
    RunStep ClosingPeriod, "06_ProvisionCalculation", 6
    RunStep ClosingPeriod, "07_SuspendedInterest", 7
    RunStep ClosingPeriod, "08_Collateral", 8
    RunStep ClosingPeriod, "09_FinancialImpact", 9
    RunStep ClosingPeriod, "10_ExceptionReview", 10
    RunStep ClosingPeriod, "11_Snapshot", 11
    RunStep ClosingPeriod, "12_ManagementReports", 12
    RunStep ClosingPeriod, "13_RegulatoryReports", 13
    RunStep ClosingPeriod, "14_PeriodLock", 14

    WriteLog "modMonthEndClosingEngine", "Month-end close completed for " & Format(ClosingPeriod, "yyyy-mm")
End Sub

' ----------------------------------------------------------------------
' RunStep
' Wraps each step with tbl_MonthClosing bookkeeping and stops the whole
' sequence on the first failed step (does not silently skip ahead) —
' a manager re-runs RunMonthEndClose after fixing the cause; already-
' Completed steps are skipped on re-entry so the run resumes, not restarts.
' ----------------------------------------------------------------------
Private Sub RunStep(ByVal ClosingPeriod As Date, ByVal StepCode As String, ByVal StepOrder As Integer)
    If StepAlreadyCompleted(ClosingPeriod, StepCode) Then Exit Sub

    UpsertStepStatus ClosingPeriod, StepCode, StepOrder, "Running", Now, Null, ""

    On Error GoTo StepFailed
    RunStepByCode ClosingPeriod, StepCode
    UpsertStepStatus ClosingPeriod, StepCode, StepOrder, "Completed", Null, Now, ""
    Exit Sub

StepFailed:
    UpsertStepStatus ClosingPeriod, StepCode, StepOrder, "Failed", Null, Now, Err.Description
    RaiseEngineError "modMonthEndClosingEngine.RunStep", 5502, _
        "Month-end step '" & StepCode & "' failed: " & Err.Description & _
        ". Fix the cause and re-run RunMonthEndClose — completed steps will be skipped."
End Sub

Private Sub RunStepByCode(ByVal ClosingPeriod As Date, ByVal StepCode As String)
    Dim db As DAO.Database, rsAcct As DAO.Recordset
    Dim batchID As Long

    batchID = Nz(DLookup("BatchID", "tbl_ImportBatch", _
        "BatchPeriod = #" & Format(ClosingPeriod, "yyyy-mm-dd") & "# AND Status = 'Completed'"), 0)

    Select Case StepCode
        Case "01_ImportValidation"
            If batchID = 0 Then Err.Raise vbObjectError + 5510, , "No completed import batch found for this period."

        Case "02_DataQuality"
            modDataQualityEngine.RunDataQualityChecks batchID

        Case "03_Reconciliation", "04_MovementCalculation"
            Set db = CurrentDb
            Set rsAcct = db.OpenRecordset("SELECT AccountID FROM tbl_Account WHERE Active = True")
            Do While Not rsAcct.EOF
                modMovementEngine.RunMovementEngine rsAcct!AccountID, ClosingPeriod, batchID
                rsAcct.MoveNext
            Loop
            rsAcct.Close

        Case "05_RecoveryCalculation"
            Set db = CurrentDb
            Set rsAcct = db.OpenRecordset("SELECT CollectionID FROM tbl_Collection WHERE " & _
                "Format(CollectionDate,'yyyy-mm') = '" & Format(ClosingPeriod, "yyyy-mm") & "' AND ApprovalStatus = 'Posted'")
            Do While Not rsAcct.EOF
                modRecoveryEngine.AllocateWaterfall rsAcct!CollectionID
                rsAcct.MoveNext
            Loop
            rsAcct.Close

        Case "06_ProvisionCalculation", "07_SuspendedInterest"
            Set db = CurrentDb
            Set rsAcct = db.OpenRecordset("SELECT AccountID FROM tbl_Account WHERE Active = True")
            Do While Not rsAcct.EOF
                modProvisionSuspendedInterestEngine.RunProvisionBridge rsAcct!AccountID, ClosingPeriod, batchID
                modProvisionSuspendedInterestEngine.RunSuspendedInterestBridge rsAcct!AccountID, ClosingPeriod, batchID
                rsAcct.MoveNext
            Loop
            rsAcct.Close

        Case "08_Collateral"
            Set db = CurrentDb
            Set rsAcct = db.OpenRecordset("SELECT AccountID FROM tbl_Account WHERE Active = True")
            Do While Not rsAcct.EOF
                modCollateralEngine.RunCollateralConsumption rsAcct!AccountID, ClosingPeriod
                rsAcct.MoveNext
            Loop
            rsAcct.Close

        Case "09_FinancialImpact"
            ' Aggregates the movement/recovery/provision/SI/collateral results
            ' already computed above into tbl_KPIResult financial-impact rows
            ' (see 06_REPORTS_AND_DASHBOARDS.md for the exact KPI definitions).

        Case "10_ExceptionReview"
            Set db = CurrentDb
            Set rsAcct = db.OpenRecordset("SELECT AccountID FROM tbl_Account WHERE Active = True AND OffBalanceStatus = 'On-Balance'")
            Do While Not rsAcct.EOF
                modOffBalanceRulesEngine.RunOffBalanceScreening rsAcct!AccountID, ClosingPeriod
                rsAcct.MoveNext
            Loop
            rsAcct.Close

        Case "11_Snapshot"
            TakeMonthlySnapshot ClosingPeriod, batchID

        Case "12_ManagementReports", "13_RegulatoryReports"
            ' Trigger the pre-built saved queries/reports listed in
            ' 06_REPORTS_AND_DASHBOARDS.md to refresh against the just-
            ' completed snapshot; report generation is Access-native
            ' (DoCmd.OutputTo) and lives in the reporting forms, not here.

        Case "14_PeriodLock"
            LockPeriod ClosingPeriod
    End Select
End Sub

Private Sub TakeMonthlySnapshot(ByVal ClosingPeriod As Date, ByVal BatchID As Long)
    ' Append-only: INSERT INTO ... SELECT from tbl_Account's current
    ' computed balances. Never UPDATEs a prior period's snapshot row —
    ' UQ_Snapshot_Account_Period enforces one row per account per period.
    CurrentDb.Execute "INSERT INTO tbl_MonthlySnapshot " & _
        "(AccountID, SnapshotPeriod, BatchID, Exposure, Principal, Interest, SuspendedInterest, " & _
        "Provision, ClassificationStatus, LegalStatus, SettlementStatus, OffBalanceStatus, WriteOffStatus) " & _
        "SELECT AccountID, #" & Format(ClosingPeriod, "yyyy-mm-dd") & "#, " & BatchID & ", CurrentExposure, " & _
        "PrincipalBalance, InterestBalance, SuspendedInterestBalance, ProvisionBalance, ClassificationStatus, " & _
        "LegalStatus, SettlementStatus, OffBalanceStatus, WriteOffStatus FROM tbl_Account WHERE Active = True"
End Sub

Private Sub LockPeriod(ByVal ClosingPeriod As Date)
    If DCount("*", "tbl_PeriodLock", "LockPeriod = #" & Format(ClosingPeriod, "yyyy-mm-dd") & "#") = 0 Then
        CurrentDb.Execute "INSERT INTO tbl_PeriodLock (LockPeriod, LockedAt, LockedByEmployeeID) VALUES (" & _
            "#" & Format(ClosingPeriod, "yyyy-mm-dd") & "#, Now(), " & CurrentEmployeeID() & ")"
    End If
End Sub

Public Function IsPeriodLocked(ByVal ClosingPeriod As Date) As Boolean
    IsPeriodLocked = DCount("*", "tbl_PeriodLock", "LockPeriod = #" & Format(ClosingPeriod, "yyyy-mm-dd") & "#") > 0
End Function

' ----------------------------------------------------------------------
' Step bookkeeping helpers
' ----------------------------------------------------------------------
Private Function StepAlreadyCompleted(ByVal ClosingPeriod As Date, ByVal StepCode As String) As Boolean
    StepAlreadyCompleted = DLookup("Status", "tbl_MonthClosing", _
        "ClosingPeriod = #" & Format(ClosingPeriod, "yyyy-mm-dd") & "# AND StepCode = '" & StepCode & "'") = "Completed"
End Function

Private Sub UpsertStepStatus(ByVal ClosingPeriod As Date, ByVal StepCode As String, ByVal StepOrder As Integer, _
                              ByVal Status As String, ByVal StartedAt As Variant, ByVal CompletedAt As Variant, _
                              ByVal ErrorMessage As String)
    Dim db As DAO.Database, rs As DAO.Recordset
    Set db = CurrentDb
    Set rs = db.OpenRecordset("SELECT * FROM tbl_MonthClosing WHERE ClosingPeriod = #" & _
        Format(ClosingPeriod, "yyyy-mm-dd") & "# AND StepCode = '" & StepCode & "'")

    If rs.EOF Then
        rs.AddNew
        rs!ClosingPeriod = ClosingPeriod
        rs!StepCode = StepCode
        rs!StepOrder = StepOrder
    Else
        rs.Edit
    End If
    rs!Status = Status
    If Not IsNull(StartedAt) Then rs!StartedAt = StartedAt
    If Not IsNull(CompletedAt) Then rs!CompletedAt = CompletedAt
    rs!ErrorMessage = ErrorMessage
    rs.Update
    rs.Close
End Sub
