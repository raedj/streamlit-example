Attribute VB_Name = "modCollateralEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modCollateralEngine
' Straight-line collateral consumption anchored to Account.DefaultDate
' (never SystemEntryDate), over a configurable number of months
' (tbl_SystemParameter 'COLLATERAL_CONSUMPTION_MONTHS', default 60).
' Runs once per collateral per account per month during month-end close;
' writes one append-only tbl_CollateralConsumption row per period so
' coverage as-of any historical month-end can be reconstructed exactly.
' ============================================================================

' ----------------------------------------------------------------------
' RunCollateralConsumption
' Call once per account during month-end close, for the period being
' closed. Iterates every eligible, active collateral on the account.
' ----------------------------------------------------------------------
Public Sub RunCollateralConsumption(ByVal AccountID As Long, ByVal SnapshotPeriod As Date)
    Dim db As DAO.Database, rsColl As DAO.Recordset
    Dim defaultDate As Variant
    Dim consumptionMonths As Long

    defaultDate = DLookup("DefaultDate", "tbl_Account", "AccountID = " & AccountID)
    If IsNull(defaultDate) Then
        WriteLog "modCollateralEngine", "Skipped Account " & AccountID & _
            ": no DefaultDate set, cannot anchor consumption schedule.", "WARN"
        Exit Sub
    End If

    consumptionMonths = GetSystemParameter("COLLATERAL_CONSUMPTION_MONTHS", SnapshotPeriod)

    Set db = CurrentDb
    Set rsColl = db.OpenRecordset( _
        "SELECT * FROM tbl_Collateral WHERE AccountID = " & AccountID & _
        " AND Active = True AND EligibleForConsumption = True")

    Do While Not rsColl.EOF
        ConsumeOneCollateral rsColl!CollateralID, rsColl!OriginalValue, CDate(defaultDate), _
                              SnapshotPeriod, consumptionMonths
        rsColl.MoveNext
    Loop
    rsColl.Close
    Set rsColl = Nothing
    Set db = Nothing
End Sub

Private Sub ConsumeOneCollateral(ByVal CollateralID As Long, ByVal OriginalValue As Currency, _
                                  ByVal DefaultDate As Date, ByVal SnapshotPeriod As Date, _
                                  ByVal ConsumptionMonths As Long)
    Dim monthsSinceDefault As Long
    Dim monthlyConsumption As Currency
    Dim consumedToDate As Currency
    Dim remainingValue As Currency

    monthsSinceDefault = DateDiff("m", DefaultDate, SnapshotPeriod)
    If monthsSinceDefault < 0 Then monthsSinceDefault = 0

    monthlyConsumption = OriginalValue / ConsumptionMonths
    consumedToDate = monthlyConsumption * IIf(monthsSinceDefault > ConsumptionMonths, ConsumptionMonths, monthsSinceDefault)
    remainingValue = OriginalValue - consumedToDate
    If remainingValue < 0 Then remainingValue = 0

    ' Upsert semantics: one row per (CollateralID, ConsumptionPeriod) —
    ' delete-then-insert is acceptable ONLY here because this table is a
    ' derived monthly calculation prior to month-end lock, not a posted
    ' financial transaction (contrast with tbl_Movement/tbl_Collection,
    ' which are strictly append-only and never touched this way).
    CurrentDb.Execute "DELETE FROM tbl_CollateralConsumption WHERE CollateralID = " & CollateralID & _
        " AND ConsumptionPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "#"

    CurrentDb.Execute "INSERT INTO tbl_CollateralConsumption " & _
        "(CollateralID, ConsumptionPeriod, MonthsSinceDefault, MonthlyConsumptionAmount, ConsumedToDate, RemainingValue) VALUES (" & _
        CollateralID & ", #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "#, " & monthsSinceDefault & ", " & _
        monthlyConsumption & ", " & consumedToDate & ", " & remainingValue & ")"
End Sub

' ----------------------------------------------------------------------
' UncoveredExposure
' Exposure - Provision - SuspendedInterest - RemainingEligibleCollateral,
' floored at zero. Summed across all of the account's eligible collateral.
' ----------------------------------------------------------------------
Public Function UncoveredExposure(ByVal AccountID As Long, ByVal SnapshotPeriod As Date) As Currency
    Dim exposure As Currency, provision As Currency, suspendedInterest As Currency
    Dim remainingCollateral As Currency
    Dim result As Currency

    exposure = Nz(DLookup("Exposure", "tbl_MonthlySnapshot", _
        "AccountID = " & AccountID & " AND SnapshotPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "#"), 0)
    provision = Nz(DLookup("Provision", "tbl_MonthlySnapshot", _
        "AccountID = " & AccountID & " AND SnapshotPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "#"), 0)
    suspendedInterest = Nz(DLookup("SuspendedInterest", "tbl_MonthlySnapshot", _
        "AccountID = " & AccountID & " AND SnapshotPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "#"), 0)

    remainingCollateral = Nz(DSum("RemainingValue", "tbl_CollateralConsumption", _
        "ConsumptionPeriod = #" & Format(SnapshotPeriod, "yyyy-mm-dd") & "# AND CollateralID IN (" & _
        "SELECT CollateralID FROM tbl_Collateral WHERE AccountID = " & AccountID & ")"), 0)

    result = exposure - provision - suspendedInterest - remainingCollateral
    If result < 0 Then result = 0
    UncoveredExposure = result
End Function
