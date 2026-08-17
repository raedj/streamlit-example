Attribute VB_Name = "modRecoveryEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modRecoveryEngine
' Financial waterfall allocation + recovery classification for every
' tbl_Collection row, and the same-month Off-Balance != Recovery guard.
' ============================================================================

Private Const WATERFALL_STEP_PRINCIPAL As String = "Principal"
Private Const WATERFALL_STEP_INTEREST As String = "Interest"
Private Const WATERFALL_STEP_SUSPENDED_INTEREST As String = "SuspendedInterest"
Private Const WATERFALL_STEP_PROVISION As String = "ProvisionImpact"
Private Const WATERFALL_STEP_WAIVER As String = "Waiver"
Private Const WATERFALL_STEP_COLLATERAL As String = "CollateralImpact"
Private Const WATERFALL_STEP_COSTS As String = "Costs"

' ----------------------------------------------------------------------
' AllocateWaterfall
' Allocates CollectionID's GrossAmount down the fixed waterfall order,
' writing one tbl_CollectionAllocation row per step actually consumed.
' Each step is capped at the account's outstanding balance for that
' component (Principal/Interest/SuspendedInterest/Provision), remainder
' rolls forward.
' ----------------------------------------------------------------------
Public Sub AllocateWaterfall(ByVal CollectionID As Long)
    Dim db As DAO.Database, rsColl As DAO.Recordset, rsAcct As DAO.Recordset
    Dim accountID As Long, remaining As Currency
    Dim stepOrder As Integer

    Set db = CurrentDb
    Set rsColl = db.OpenRecordset("SELECT * FROM tbl_Collection WHERE CollectionID = " & CollectionID)
    If rsColl.EOF Then
        RaiseEngineError "modRecoveryEngine.AllocateWaterfall", 5201, "CollectionID " & CollectionID & " not found."
    End If

    accountID = rsColl!AccountID
    remaining = rsColl!GrossAmount
    stepOrder = 1

    Set rsAcct = db.OpenRecordset("SELECT * FROM tbl_Account WHERE AccountID = " & accountID)

    remaining = AllocateStep(CollectionID, WATERFALL_STEP_PRINCIPAL, remaining, Nz(rsAcct!PrincipalBalance, 0), stepOrder)
    remaining = AllocateStep(CollectionID, WATERFALL_STEP_INTEREST, remaining, Nz(rsAcct!InterestBalance, 0), stepOrder)
    remaining = AllocateStep(CollectionID, WATERFALL_STEP_SUSPENDED_INTEREST, remaining, Nz(rsAcct!SuspendedInterestBalance, 0), stepOrder)
    remaining = AllocateStep(CollectionID, WATERFALL_STEP_PROVISION, remaining, Nz(rsAcct!ProvisionBalance, 0), stepOrder)
    ' Waiver / Collateral Impact / Costs are not drawn from GrossAmount cash —
    ' they are recorded separately (tbl_Waiver, tbl_CollateralConsumption,
    ' tbl_RecoveryCost) and referenced here only for waterfall visibility,
    ' at zero incremental cash allocation.
    AllocateStep CollectionID, WATERFALL_STEP_WAIVER, 0, 0, stepOrder
    AllocateStep CollectionID, WATERFALL_STEP_COLLATERAL, 0, 0, stepOrder

    Dim totalCosts As Currency
    totalCosts = Nz(DSum("Amount", "tbl_RecoveryCost", "CollectionID = " & CollectionID), 0)
    AllocateStep CollectionID, WATERFALL_STEP_COSTS, totalCosts, totalCosts, stepOrder

    rsColl.Close
    rsAcct.Close
    Set rsColl = Nothing
    Set rsAcct = Nothing
End Sub

Private Function AllocateStep(ByVal CollectionID As Long, ByVal StepName As String, _
                               ByVal RemainingBefore As Currency, ByVal Capacity As Currency, _
                               ByRef StepOrder As Integer) As Currency
    Dim allocated As Currency
    allocated = IIf(RemainingBefore < Capacity, RemainingBefore, Capacity)
    If allocated <> 0 Then
        CurrentDb.Execute "INSERT INTO tbl_CollectionAllocation " & _
            "(CollectionID, WaterfallStep, Amount, StepOrder) VALUES (" & _
            CollectionID & ", '" & StepName & "', " & allocated & ", " & StepOrder & ")"
    End If
    StepOrder = StepOrder + 1
    AllocateStep = RemainingBefore - allocated
End Function

' ----------------------------------------------------------------------
' NetRecovery(CollectionID)
'   = GrossAmount - SUM(tbl_RecoveryCost.Amount)
' ----------------------------------------------------------------------
Public Function NetRecovery(ByVal CollectionID As Long) As Currency
    Dim gross As Currency, costs As Currency
    gross = Nz(DLookup("GrossAmount", "tbl_Collection", "CollectionID = " & CollectionID), 0)
    costs = Nz(DSum("Amount", "tbl_RecoveryCost", "CollectionID = " & CollectionID), 0)
    NetRecovery = gross - costs
End Function

' ----------------------------------------------------------------------
' ClassifyMovement
' Determines RecoveryClassification from CollectionType and sets
' IsRecovery. Off-Balance/WriteOff/Waiver movement types are never
' classified as recovery here — those are posted directly via their own
' tables (tbl_OffBalance / tbl_WriteOff / tbl_Waiver), not through
' tbl_Collection, so this function only ever sees genuine recovery events.
' ----------------------------------------------------------------------
Public Function ClassifyMovement(ByVal CollectionType As String) As String
    Select Case CollectionType
        Case "Cash Collection":                    ClassifyMovement = "Cash"
        Case "Auction – Vehicle", "Auction - Vehicle": ClassifyMovement = "Auction"
        Case "Auction – Real Estate", "Auction - Real Estate": ClassifyMovement = "Auction"
        Case "Voluntary Property Transfer":         ClassifyMovement = "VoluntaryTransfer"
        Case "Collateral Sale":                     ClassifyMovement = "Collateral"
        Case "Insurance Recovery":                  ClassifyMovement = "Insurance"
        Case "Guarantor Recovery":                  ClassifyMovement = "Guarantor"
        Case "Legal Recovery":                      ClassifyMovement = "Legal"
        Case "Settlement Payment":                  ClassifyMovement = "Settlement"
        Case "Rescheduling-related Collection":     ClassifyMovement = "Other"
        Case Else:                                  ClassifyMovement = "Other"
    End Select
End Function

' ----------------------------------------------------------------------
' ApplySameMonthOffBalanceRule
' Called from the Off-Balance posting path (modOffBalanceRulesEngine /
' the manual Off-Balance form), not from the recovery path — this is the
' single place a MovementType='OffBalance' row is ever written, so
' IsRecovery=False is guaranteed there regardless of the amount.
' ----------------------------------------------------------------------
Public Sub ApplySameMonthOffBalanceRule(ByVal AccountID As Long, ByVal OffBalanceDate As Date, _
                                         ByVal ExposureReduced As Currency, ByVal BatchID As Long)
    Dim movementPeriod As Date
    movementPeriod = DateSerial(Year(OffBalanceDate), Month(OffBalanceDate), 1)

    CurrentDb.Execute "INSERT INTO tbl_Movement " & _
        "(AccountID, MovementPeriod, MovementType, MovementCategory, Amount, IsRecovery, BatchID) VALUES (" & _
        AccountID & ", #" & Format(movementPeriod, "yyyy-mm-dd") & "#, 'OffBalance', 'Portfolio', " & _
        ExposureReduced & ", False, " & BatchID & ")"

    LogAuditEvent ActionType:="Insert", TableName:="tbl_Movement", RecordID:=0, AccountID:=AccountID, _
        Reason:="Same-month Off-Balance movement recorded as Portfolio reduction; explicitly NOT Recovery.", _
        SourceContext:="modRecoveryEngine.ApplySameMonthOffBalanceRule"
End Sub
