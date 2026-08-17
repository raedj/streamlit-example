Attribute VB_Name = "modMovementEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modMovementEngine
' Opening -> Closing balance bridge per account per month; raises
' tbl_MovementException when the bridge doesn't tie to the source file's
' reported closing balance. Never infers a collection from an unexplained
' gap (see 04_ENGINES_SPEC.md).
' ============================================================================

' ----------------------------------------------------------------------
' RunMovementEngine
' Called once per account during month-end close, for the period being
' closed. Batch-oriented: the month-end orchestrator loops accounts, not
' this procedure looping forms.
' ----------------------------------------------------------------------
Public Sub RunMovementEngine(ByVal AccountID As Long, ByVal MovementPeriod As Date, ByVal BatchID As Long)
    Dim opening As Currency, newMovements As Currency
    Dim collections As Currency, waivers As Currency
    Dim writeOffs As Currency, offBalanceReductions As Currency, otherReductions As Currency
    Dim expectedClosing As Currency, actualClosing As Currency
    Dim tolerance As Currency

    opening = GetOpeningBalance(AccountID, MovementPeriod)
    newMovements = SumMovementType(AccountID, MovementPeriod, "NewMovement")
    collections = SumMovementType(AccountID, MovementPeriod, "Collection")
    waivers = SumMovementType(AccountID, MovementPeriod, "Waiver")
    writeOffs = SumMovementType(AccountID, MovementPeriod, "WriteOff")
    offBalanceReductions = SumMovementType(AccountID, MovementPeriod, "OffBalance")
    otherReductions = SumMovementType(AccountID, MovementPeriod, "OtherReduction")

    expectedClosing = opening + newMovements - collections - waivers - writeOffs _
                       - offBalanceReductions - otherReductions

    actualClosing = GetSourceReportedClosing(AccountID, MovementPeriod)
    tolerance = GetSystemParameter("MOVEMENT_TOLERANCE", MovementPeriod)

    RecordClosingMovement AccountID, MovementPeriod, expectedClosing, BatchID

    If Abs(expectedClosing - actualClosing) > tolerance Then
        RaiseMovementException AccountID, MovementPeriod, expectedClosing, actualClosing
    End If
End Sub

Private Function GetOpeningBalance(ByVal AccountID As Long, ByVal MovementPeriod As Date) As Currency
    Dim priorClosing As Variant
    priorClosing = DLookup("Amount", "tbl_Movement", _
        "AccountID = " & AccountID & " AND MovementType = 'Closing' AND MovementPeriod = #" & _
        Format(DateAdd("m", -1, MovementPeriod), "yyyy-mm-dd") & "#")
    If IsNull(priorClosing) Then
        GetOpeningBalance = 0   ' first period the account appears in the system
    Else
        GetOpeningBalance = priorClosing
    End If
End Function

Private Function SumMovementType(ByVal AccountID As Long, ByVal MovementPeriod As Date, ByVal MovementType As String) As Currency
    SumMovementType = Nz(DSum("Amount", "tbl_Movement", _
        "AccountID = " & AccountID & " AND MovementPeriod = #" & Format(MovementPeriod, "yyyy-mm-dd") & _
        "# AND MovementType = '" & MovementType & "'"), 0)
End Function

Private Function GetSourceReportedClosing(ByVal AccountID As Long, ByVal MovementPeriod As Date) As Currency
    Dim acctNumber As String
    acctNumber = Nz(DLookup("AccountNumber", "tbl_Account", "AccountID = " & AccountID), "")
    GetSourceReportedClosing = Nz(DLookup("Exposure", "tbl_MonthlySnapshot", _
        "AccountID = " & AccountID & " AND SnapshotPeriod = #" & Format(MovementPeriod, "yyyy-mm-dd") & "#"), 0)
End Function

Private Sub RecordClosingMovement(ByVal AccountID As Long, ByVal MovementPeriod As Date, _
                                   ByVal ClosingAmount As Currency, ByVal BatchID As Long)
    Dim db As DAO.Database, rs As DAO.Recordset
    Set db = CurrentDb
    Set rs = db.OpenRecordset("tbl_Movement", dbOpenDynaset, dbAppendOnly)
    rs.AddNew
    rs!AccountID = AccountID
    rs!MovementPeriod = MovementPeriod
    rs!MovementType = "Closing"
    rs!MovementCategory = "Portfolio"
    rs!Amount = ClosingAmount
    rs!IsRecovery = False
    rs!BatchID = BatchID
    rs.Update
    rs.Close
End Sub

' ----------------------------------------------------------------------
' RaiseMovementException
' The difference is recorded as-is; it is explicitly NOT written to
' tbl_Movement as a Collection or any recovery-flagged type. A reviewer
' resolves it through tbl_MovementException.Status, and only a subsequent,
' properly-sourced movement (e.g. a corrected import) changes the balance.
' ----------------------------------------------------------------------
Private Sub RaiseMovementException(ByVal AccountID As Long, ByVal MovementPeriod As Date, _
                                    ByVal ExpectedClosing As Currency, ByVal ActualClosing As Currency)
    CurrentDb.Execute "INSERT INTO tbl_MovementException " & _
        "(AccountID, MovementPeriod, ExpectedClosing, ActualClosing, Difference, ExceptionType, Status) VALUES (" & _
        AccountID & ", #" & Format(MovementPeriod, "yyyy-mm-dd") & "#, " & _
        ExpectedClosing & ", " & ActualClosing & ", " & (ExpectedClosing - ActualClosing) & _
        ", 'Unexplained Movement', 'Open')"

    LogAuditEvent ActionType:="Insert", TableName:="tbl_MovementException", RecordID:=0, _
        AccountID:=AccountID, Reason:="Auto-raised by modMovementEngine: expected/actual closing mismatch", _
        SourceContext:="modMovementEngine.RaiseMovementException"

    WriteLog "modMovementEngine", "Unexplained movement: Account " & AccountID & " period " & _
        Format(MovementPeriod, "yyyy-mm") & " expected=" & ExpectedClosing & " actual=" & ActualClosing, "WARN"
End Sub
