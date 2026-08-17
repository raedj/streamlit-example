Attribute VB_Name = "modAuditTrail"
Option Explicit
Option Compare Database

' ============================================================================
' modAuditTrail
' Central audit logger + the "reversal, not deletion" guard used by every
' other engine before it touches a row that has already been posted.
' ============================================================================

' ----------------------------------------------------------------------
' LogAuditEvent
' Call this immediately after (never before, in case the write fails)
' any insert/update to a financially-impactful table.
' ----------------------------------------------------------------------
Public Sub LogAuditEvent(ByVal ActionType As String, ByVal TableName As String, _
                          ByVal RecordID As Long, Optional ByVal AccountID As Variant, _
                          Optional ByVal FieldName As String = "", _
                          Optional ByVal OldValue As Variant, Optional ByVal NewValue As Variant, _
                          Optional ByVal Reason As String = "", _
                          Optional ByVal ApprovalID As Variant, _
                          Optional ByVal SourceContext As String = "")
    Dim db As DAO.Database
    Dim rs As DAO.Recordset

    Set db = CurrentDb
    Set rs = db.OpenRecordset("tbl_AuditTrail", dbOpenDynaset, dbAppendOnly)

    rs.AddNew
    rs!EventDateTime = Now
    rs!UserName = Environ("USERNAME")
    rs!EmployeeID = CurrentEmployeeID()
    rs!ActionType = ActionType
    rs!TableName = TableName
    rs!RecordID = RecordID
    If Not IsMissing(AccountID) Then rs!AccountID = AccountID
    rs!FieldName = FieldName
    If Not IsMissing(OldValue) Then rs!OldValue = NzToText(OldValue)
    If Not IsMissing(NewValue) Then rs!NewValue = NzToText(NewValue)
    rs!Reason = Reason
    If Not IsMissing(ApprovalID) Then rs!ApprovalID = ApprovalID
    rs!SourceContext = SourceContext
    rs.Update

    rs.Close
    Set rs = Nothing
    Set db = Nothing
End Sub

Private Function NzToText(ByVal v As Variant) As String
    If IsNull(v) Then
        NzToText = "<NULL>"
    Else
        NzToText = CStr(v)
    End If
End Function

' ----------------------------------------------------------------------
' AssertNotPosted
' Every UPDATE/DELETE path in the system must call this before touching a
' row. If the row's ApprovalStatus is 'Posted' (i.e. it has already been
' approved and has affected balances), the write is blocked outright —
' the only legitimate way to undo it is CreateReversal in
' modApprovalWorkflowEngine, which posts an equal-and-opposite entry and
' leaves the original row intact for audit.
' ----------------------------------------------------------------------
Public Sub AssertNotPosted(ByVal TableName As String, ByVal RecordID As Long, _
                            ByVal CurrentApprovalStatus As String)
    If CurrentApprovalStatus = "Posted" Then
        RaiseEngineError "modAuditTrail.AssertNotPosted", 5101, _
            "Blocked: " & TableName & "." & RecordID & " is already Posted. " & _
            "Use modApprovalWorkflowEngine.CreateReversal to correct a posted entry " & _
            "instead of editing or deleting it."
    End If
End Sub
