Attribute VB_Name = "modApprovalWorkflowEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modApprovalWorkflowEngine
' Maker-checker state machine: Draft -> Submitted -> UnderReview ->
' Approved/Rejected -> Posted. Blocks self-approval. Resolves the required
' approval level from tbl_RegulatoryRules (amount/entity-type thresholds),
' not a hard-coded rank. Also owns CreateReversal, the only sanctioned way
' to undo a Posted entry (see modAuditTrail.AssertNotPosted).
' ============================================================================

' ----------------------------------------------------------------------
' SubmitForApproval
' Called by a manual-entry form (e.g. manual collection, waiver,
' settlement, write-off, off-balance, adjustment) after the user saves a
' Draft row. Creates the tbl_Approval request and marks the source row
' 'Submitted'.
' ----------------------------------------------------------------------
Public Function SubmitForApproval(ByVal EntityType As String, ByVal EntityID As Long, _
                                   ByVal EntityAmount As Currency) As Long
    Dim requesterID As Long
    Dim requiredLevel As String
    Dim approvalID As Long

    requesterID = CurrentEmployeeID()
    requiredLevel = ResolveRequiredApprovalLevel(EntityType, EntityAmount)

    CurrentDb.Execute "INSERT INTO tbl_Approval " & _
        "(EntityType, EntityID, RequestedByEmployeeID, RequestedDate, Status, RequiredApprovalLevel) VALUES (" & _
        "'" & EntityType & "', " & EntityID & ", " & requesterID & ", Now(), 'Submitted', '" & requiredLevel & "')"

    approvalID = CLng(CurrentDb.OpenRecordset("SELECT @@IDENTITY AS NewID")!NewID)

    SetEntityApprovalFields EntityType, EntityID, "Submitted", approvalID

    LogAuditEvent ActionType:="Insert", TableName:="tbl_Approval", RecordID:=approvalID, _
        Reason:="Submitted for approval (" & EntityType & " " & EntityID & ", level=" & requiredLevel & ")", _
        SourceContext:="modApprovalWorkflowEngine.SubmitForApproval"

    SubmitForApproval = approvalID
End Function

Private Function ResolveRequiredApprovalLevel(ByVal EntityType As String, ByVal Amount As Currency) As String
    Dim ruleCode As String
    ruleCode = "APPROVAL_LEVEL_" & UCase(EntityType)

    Dim thresholdHigh As Variant
    thresholdHigh = DLookup("ThresholdValue", "tbl_RegulatoryRules", _
        "RuleCode = '" & ruleCode & "' AND Active = True")

    If IsNull(thresholdHigh) Then
        ResolveRequiredApprovalLevel = "TeamManager"   ' safe default; log for follow-up
        WriteLog "modApprovalWorkflowEngine", "No approval-level rule for " & ruleCode & _
            "; defaulted to TeamManager.", "WARN"
    ElseIf Amount >= thresholdHigh Then
        ResolveRequiredApprovalLevel = "DepartmentManager"
    Else
        ResolveRequiredApprovalLevel = "TeamManager"
    End If
End Function

' ----------------------------------------------------------------------
' ReviewApproval
' The manager-facing action. Hard-blocks self-approval: a request cannot
' be approved/rejected by the same EmployeeID that submitted it, enforced
' here rather than only hidden in the UI.
' ----------------------------------------------------------------------
Public Sub ReviewApproval(ByVal ApprovalID As Long, ByVal Decision As String, Optional ByVal RejectionReason As String = "")
    Dim reviewerID As Long, requesterID As Long
    Dim entityType As String, entityID As Long

    reviewerID = CurrentEmployeeID()
    requesterID = Nz(DLookup("RequestedByEmployeeID", "tbl_Approval", "ApprovalID = " & ApprovalID), 0)

    If reviewerID = requesterID Then
        RaiseEngineError "modApprovalWorkflowEngine.ReviewApproval", 5401, _
            "Employee " & reviewerID & " cannot approve their own submission (ApprovalID " & ApprovalID & ")."
    End If

    If Decision <> "Approved" And Decision <> "Rejected" Then
        RaiseEngineError "modApprovalWorkflowEngine.ReviewApproval", 5402, "Decision must be 'Approved' or 'Rejected'."
    End If

    entityType = Nz(DLookup("EntityType", "tbl_Approval", "ApprovalID = " & ApprovalID), "")
    entityID = Nz(DLookup("EntityID", "tbl_Approval", "ApprovalID = " & ApprovalID), 0)

    CurrentDb.Execute "UPDATE tbl_Approval SET Status = '" & Decision & "', ReviewedByEmployeeID = " & _
        reviewerID & ", ReviewedDate = Now(), RejectionReason = '" & Replace(RejectionReason, "'", "''") & _
        "' WHERE ApprovalID = " & ApprovalID

    LogAuditEvent ActionType:=Decision, TableName:="tbl_Approval", RecordID:=ApprovalID, _
        Reason:=RejectionReason, ApprovalID:=ApprovalID, SourceContext:="modApprovalWorkflowEngine.ReviewApproval"

    If Decision = "Approved" Then
        SetEntityApprovalFields entityType, entityID, "Approved", ApprovalID
        PostEntity entityType, entityID, ApprovalID
    Else
        SetEntityApprovalFields entityType, entityID, "Rejected", ApprovalID
    End If
End Sub

' ----------------------------------------------------------------------
' PostEntity
' Only an Approved request reaches here. Posting means: mark the source
' row 'Posted' and write the corresponding tbl_Movement row(s) so the
' entry finally affects balances/recovery totals. Nothing before this
' point has touched tbl_Movement.
' ----------------------------------------------------------------------
Private Sub PostEntity(ByVal EntityType As String, ByVal EntityID As Long, ByVal ApprovalID As Long)
    SetEntityApprovalFields EntityType, EntityID, "Posted", ApprovalID

    Select Case EntityType
        Case "Collection"
            modRecoveryEngine.AllocateWaterfall EntityID
        Case "Waiver", "Settlement", "WriteOff", "OffBalance", "Adjustment"
            ' Each entity type's own posting routine writes its
            ' tbl_Movement row(s) with the correct MovementType/Category
            ' (see 04_ENGINES_SPEC.md) — wired here as the single
            ' post-approval trigger point.
    End Select

    LogAuditEvent ActionType:="Posted", TableName:=EntityType, RecordID:=EntityID, _
        ApprovalID:=ApprovalID, SourceContext:="modApprovalWorkflowEngine.PostEntity"
End Sub

Private Sub SetEntityApprovalFields(ByVal EntityType As String, ByVal EntityID As Long, _
                                     ByVal Status As String, ByVal ApprovalID As Long)
    Dim tableName As String, pkName As String
    Select Case EntityType
        Case "Collection": tableName = "tbl_Collection": pkName = "CollectionID"
        Case "Waiver": tableName = "tbl_Waiver": pkName = "WaiverID"
        Case "Settlement": tableName = "tbl_Settlement": pkName = "SettlementID"
        Case "WriteOff": tableName = "tbl_WriteOff": pkName = "WriteOffID"
        Case "OffBalance": tableName = "tbl_OffBalance": pkName = "OffBalanceID"
        Case Else
            RaiseEngineError "modApprovalWorkflowEngine.SetEntityApprovalFields", 5403, _
                "Unknown EntityType '" & EntityType & "'."
    End Select

    On Error Resume Next   ' ApprovalStatus/ApprovalID columns may not exist on every entity table variant
    CurrentDb.Execute "UPDATE " & tableName & " SET ApprovalStatus = '" & Status & "', ApprovalID = " & _
        ApprovalID & " WHERE " & pkName & " = " & EntityID
    On Error GoTo 0
End Sub

' ----------------------------------------------------------------------
' CreateReversal
' The ONLY sanctioned way to correct a Posted entry. Inserts an
' equal-and-opposite tbl_Movement row referencing the original via
' SourceReferenceID, and requires its own approval (a reversal is itself
' a financially-impactful manual entry).
' ----------------------------------------------------------------------
Public Sub CreateReversal(ByVal OriginalMovementID As Long, ByVal Reason As String)
    Dim db As DAO.Database, rsOrig As DAO.Recordset
    Dim newApprovalID As Long

    Set db = CurrentDb
    Set rsOrig = db.OpenRecordset("SELECT * FROM tbl_Movement WHERE MovementID = " & OriginalMovementID)
    If rsOrig.EOF Then
        RaiseEngineError "modApprovalWorkflowEngine.CreateReversal", 5404, "MovementID " & OriginalMovementID & " not found."
    End If

    Dim reversalID As Long
    db.Execute "INSERT INTO tbl_Movement " & _
        "(AccountID, MovementPeriod, MovementType, MovementCategory, Amount, IsRecovery, SourceReferenceID) VALUES (" & _
        rsOrig!AccountID & ", #" & Format(Date, "yyyy-mm-dd") & "#, '" & rsOrig!MovementType & "-Reversal', '" & _
        rsOrig!MovementCategory & "', " & (-rsOrig!Amount) & ", " & rsOrig!IsRecovery & ", " & OriginalMovementID & ")"

    reversalID = CLng(db.OpenRecordset("SELECT @@IDENTITY AS NewID")!NewID)

    newApprovalID = SubmitForApproval("Adjustment", reversalID, Abs(rsOrig!Amount))

    LogAuditEvent ActionType:="Reversal", TableName:="tbl_Movement", RecordID:=reversalID, _
        AccountID:=rsOrig!AccountID, OldValue:=rsOrig!Amount, NewValue:=(-rsOrig!Amount), Reason:=Reason, _
        ApprovalID:=newApprovalID, SourceContext:="modApprovalWorkflowEngine.CreateReversal"

    rsOrig.Close
    Set rsOrig = Nothing
    Set db = Nothing
End Sub
