Attribute VB_Name = "modProvisionSuspendedInterestEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modProvisionSuspendedInterestEngine
' Opening->Closing bridges for Provision and Suspended Interest, and the
' Profit-vs-Saving split on any reversal/release (spec §23): a reversal of
' a balance that existed at the prior 31-Dec is Profit; a reversal of a
' balance formed and released within the current year is Saving. The two
' are always stored separately, never pre-summed.
' ============================================================================

Public Sub RunProvisionBridge(ByVal AccountID As Long, ByVal SnapshotPeriod As Date, ByVal BatchID As Long)
    Dim opening As Currency, increase As Currency, decrease As Currency
    Dim utilization As Currency, release As Currency, closing As Currency
    Dim releaseClass As String

    opening = GetPriorClosing("tbl_ProvisionSnapshot", "ClosingProvision", AccountID, SnapshotPeriod)
    increase = SourceMovementAmount(AccountID, SnapshotPeriod, "ProvisionIncrease")
    decrease = SourceMovementAmount(AccountID, SnapshotPeriod, "ProvisionDecrease")
    utilization = SourceMovementAmount(AccountID, SnapshotPeriod, "ProvisionUtilization")
    release = SourceMovementAmount(AccountID, SnapshotPeriod, "ProvisionRelease")

    closing = opening + increase - decrease - utilization - release

    If release <> 0 Then
        releaseClass = ClassifyProfitOrSaving(AccountID, SnapshotPeriod, opening)
    Else
        releaseClass = Null
    End If

    UpsertBridgeRow "tbl_ProvisionSnapshot", AccountID, SnapshotPeriod, Array( _
        "OpeningProvision", opening, "ProvisionIncrease", increase, "ProvisionDecrease", decrease, _
        "ProvisionUtilization", utilization, "ProvisionRelease", release, "ClosingProvision", closing, _
        "ReleaseClassification", releaseClass)
End Sub

Public Sub RunSuspendedInterestBridge(ByVal AccountID As Long, ByVal SnapshotPeriod As Date, ByVal BatchID As Long)
    Dim opening As Currency, newSuspension As Currency, recovery As Currency
    Dim waiver As Currency, reversal As Currency, closing As Currency
    Dim reversalClass As String

    opening = GetPriorClosing("tbl_SuspendedInterest", "ClosingBalance", AccountID, SnapshotPeriod)
    newSuspension = SourceMovementAmount(AccountID, SnapshotPeriod, "SI_New")
    recovery = SourceMovementAmount(AccountID, SnapshotPeriod, "SI_Recovery")
    waiver = SourceMovementAmount(AccountID, SnapshotPeriod, "SI_Waiver")
    reversal = SourceMovementAmount(AccountID, SnapshotPeriod, "SI_Reversal")

    closing = opening + newSuspension - recovery - waiver - reversal

    If reversal <> 0 Then
        reversalClass = ClassifyProfitOrSaving(AccountID, SnapshotPeriod, opening)
    Else
        reversalClass = Null
    End If

    UpsertBridgeRow "tbl_SuspendedInterest", AccountID, SnapshotPeriod, Array( _
        "OpeningBalance", opening, "NewSuspension", newSuspension, "Recovery", recovery, _
        "Waiver", waiver, "Reversal", reversal, "ClosingBalance", closing, _
        "ReversalClassification", reversalClass)
End Sub

' ----------------------------------------------------------------------
' ClassifyProfitOrSaving
' "Profit": the opening balance being reversed already existed at the
' prior 31-December (i.e. it was on the books before this fiscal year).
' "Saving": the balance being reversed was itself formed during the
' current fiscal year (created and released within the same year).
' This is a simplification for a straightforward opening-balance test;
' where a bank's policy requires layer-by-layer (vintage) tracking of
' exactly which prior-year vs current-year slice is being released, this
' is the extension point.
' ----------------------------------------------------------------------
Public Function ClassifyProfitOrSaving(ByVal AccountID As Long, ByVal SnapshotPeriod As Date, _
                                        ByVal OpeningBalance As Currency) As String
    Dim priorDec31 As Date
    priorDec31 = DateSerial(Year(SnapshotPeriod) - 1, 12, 31)

    If OpeningBalance > 0 And priorDec31 < SnapshotPeriod Then
        ' Opening balance carried in from before this year existed at the
        ' prior year-end snapshot => releasing it is Profit.
        ClassifyProfitOrSaving = "Profit"
    Else
        ClassifyProfitOrSaving = "Saving"
    End If
End Function

' ----------------------------------------------------------------------
' Shared bridge helpers
' ----------------------------------------------------------------------
Private Function GetPriorClosing(ByVal TableName As String, ByVal ClosingFieldName As String, _
                                  ByVal AccountID As Long, ByVal SnapshotPeriod As Date) As Currency
    Dim priorPeriod As Date
    priorPeriod = DateAdd("m", -1, SnapshotPeriod)
    GetPriorClosing = Nz(DLookup(ClosingFieldName, TableName, _
        "AccountID = " & AccountID & " AND SnapshotPeriod = #" & Format(priorPeriod, "yyyy-mm-dd") & "#"), 0)
End Function

Private Function SourceMovementAmount(ByVal AccountID As Long, ByVal SnapshotPeriod As Date, _
                                       ByVal ComponentCode As String) As Currency
    ' Sources the period's raw movement component from tbl_Movement /
    ' staged import data, tagged by ComponentCode during staging-to-
    ' permanent promotion (modDataQualityEngine). Left as a thin lookup
    ' here so the bridge math above is the single place that combines them.
    SourceMovementAmount = Nz(DSum("Amount", "tbl_Movement", _
        "AccountID = " & AccountID & " AND MovementPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & _
        "# AND MovementType = '" & ComponentCode & "'"), 0)
End Function

Private Sub UpsertBridgeRow(ByVal TableName As String, ByVal AccountID As Long, ByVal SnapshotPeriod As Date, _
                             ByVal FieldValuePairs As Variant)
    Dim db As DAO.Database, rs As DAO.Recordset
    Dim i As Integer

    Set db = CurrentDb
    Set rs = db.OpenRecordset("SELECT * FROM " & TableName & " WHERE AccountID = " & AccountID & _
        " AND SnapshotPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "#")

    If rs.EOF Then
        rs.AddNew
        rs!AccountID = AccountID
        rs!SnapshotPeriod = SnapshotPeriod
    Else
        ' A bridge row for this account/period exists only if month-end is
        ' being re-run before period lock; AssertNotPosted-equivalent check
        ' is the period lock itself (tbl_PeriodLock), enforced by the
        ' month-end orchestrator before this sub is ever called.
        rs.Edit
    End If

    For i = 0 To UBound(FieldValuePairs) Step 2
        rs.Fields(FieldValuePairs(i)).Value = FieldValuePairs(i + 1)
    Next i
    rs.Update
    rs.Close
    Set rs = Nothing
    Set db = Nothing
End Sub
