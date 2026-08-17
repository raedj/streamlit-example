Attribute VB_Name = "modDataQualityEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modDataQualityEngine
' Validates staged rows before they are promoted to tbl_MonthlySnapshot /
' tbl_Collection. Every check below writes one tbl_DataQuality summary row
' and, per failing row, one tbl_ImportError row — so a rejected batch is
' always explainable, not a silent drop.
' ============================================================================

Public Sub RunDataQualityChecks(ByVal BatchID As Long)
    Dim totalRows As Long
    Dim failedRows As Long

    CheckDuplicateAccounts BatchID
    CheckMissingCIF BatchID
    CheckInvalidDates BatchID
    CheckNegativeValues BatchID
    CheckCollectionExceedsOutstanding BatchID
    CheckProvisionExceedsExposure BatchID
    CheckMissingDefaultDate BatchID
    CheckMissingClassification BatchID

    totalRows = DCount("*", "stg_AccountSnapshot", "BatchID = " & BatchID)
    failedRows = DCount("*", "stg_AccountSnapshot", "BatchID = " & BatchID & " AND ValidationStatus = 'Invalid'")

    Dim score As Double
    If totalRows = 0 Then
        score = 0
    Else
        score = (1 - (failedRows / totalRows)) * 100
    End If

    CurrentDb.Execute "UPDATE tbl_ImportBatch SET DataQualityScore = " & _
                       Replace(score, ",", ".") & " WHERE BatchID = " & BatchID

    WriteLog "modDataQualityEngine", "Batch " & BatchID & " DQ score = " & Format(score, "0.0") & "%"
End Sub

' ----------------------------------------------------------------------
' Each Check* procedure: (1) records a tbl_DataQuality row with the
' rows-checked/rows-failed counts, (2) flags offending stg rows
' ValidationStatus = 'Invalid' with a tbl_ImportError explaining why.
' Rows are marked invalid, never deleted, so a reviewer can see exactly
' what was excluded and why.
' ----------------------------------------------------------------------

Private Sub CheckDuplicateAccounts(ByVal BatchID As Long)
    RunCheck BatchID, "DUPLICATE_ACCOUNT", _
        "SELECT StagingID, AccountNumber FROM stg_AccountSnapshot s " & _
        "WHERE BatchID = " & BatchID & " AND (SELECT COUNT(*) FROM stg_AccountSnapshot s2 " & _
        "WHERE s2.BatchID = s.BatchID AND s2.AccountNumber = s.AccountNumber) > 1", _
        "Duplicate AccountNumber within the same import batch."
End Sub

Private Sub CheckMissingCIF(ByVal BatchID As Long)
    RunCheck BatchID, "MISSING_CIF", _
        "SELECT StagingID, AccountNumber FROM stg_AccountSnapshot " & _
        "WHERE BatchID = " & BatchID & " AND (CIF IS NULL OR CIF = '')", _
        "Missing CIF."
End Sub

Private Sub CheckInvalidDates(ByVal BatchID As Long)
    Dim db As DAO.Database, rs As DAO.Recordset
    Set db = CurrentDb
    Set rs = db.OpenRecordset( _
        "SELECT StagingID, SnapshotPeriodText FROM stg_AccountSnapshot WHERE BatchID = " & BatchID)
    Dim failCount As Long, total As Long
    Do While Not rs.EOF
        total = total + 1
        If Not IsDate(rs!SnapshotPeriodText) Then
            failCount = failCount + 1
            FlagInvalid rs!StagingID, "INVALID_DATE", "SnapshotPeriodText is not a valid date: '" & rs!SnapshotPeriodText & "'"
        End If
        rs.MoveNext
    Loop
    rs.Close
    RecordDQResult BatchID, "INVALID_DATE", total, failCount
End Sub

Private Sub CheckNegativeValues(ByVal BatchID As Long)
    Dim db As DAO.Database, rs As DAO.Recordset
    Set db = CurrentDb
    Set rs = db.OpenRecordset( _
        "SELECT StagingID, ExposureText, ProvisionText FROM stg_AccountSnapshot WHERE BatchID = " & BatchID)
    Dim failCount As Long, total As Long
    Do While Not rs.EOF
        total = total + 1
        If (IsNumeric(rs!ExposureText) And CDbl(rs!ExposureText) < 0) _
           Or (IsNumeric(rs!ProvisionText) And CDbl(rs!ProvisionText) < 0) Then
            failCount = failCount + 1
            FlagInvalid rs!StagingID, "NEGATIVE_VALUE", "Negative Exposure or Provision reported by source."
        End If
        rs.MoveNext
    Loop
    rs.Close
    RecordDQResult BatchID, "NEGATIVE_VALUE", total, failCount
End Sub

Private Sub CheckCollectionExceedsOutstanding(ByVal BatchID As Long)
    ' Compares stg_Collection.AmountText for the batch's accounts against
    ' the account's current outstanding balance (tbl_Account). A collection
    ' larger than outstanding is not auto-rejected (settlements can
    ' legitimately include third-party top-ups) but is always flagged for
    ' review rather than silently accepted.
    RunCheck BatchID, "COLLECTION_EXCEEDS_OUTSTANDING", _
        "SELECT c.StagingID, c.AccountNumber FROM stg_Collection c " & _
        "INNER JOIN tbl_Account a ON a.AccountNumber = c.AccountNumber " & _
        "WHERE c.BatchID = " & BatchID & " AND IsNumeric(c.AmountText) " & _
        "AND CDbl(c.AmountText) > Nz(a.CurrentExposure,0) * 1.05", _
        "Collection amount exceeds account outstanding exposure by more than 5%."
End Sub

Private Sub CheckProvisionExceedsExposure(ByVal BatchID As Long)
    Dim db As DAO.Database, rs As DAO.Recordset
    Set db = CurrentDb
    Set rs = db.OpenRecordset( _
        "SELECT StagingID, ExposureText, ProvisionText FROM stg_AccountSnapshot WHERE BatchID = " & BatchID)
    Dim failCount As Long, total As Long
    Do While Not rs.EOF
        total = total + 1
        If IsNumeric(rs!ExposureText) And IsNumeric(rs!ProvisionText) Then
            If CDbl(rs!ProvisionText) > CDbl(rs!ExposureText) Then
                failCount = failCount + 1
                FlagInvalid rs!StagingID, "PROVISION_EXCEEDS_EXPOSURE", "Provision exceeds reported Exposure."
            End If
        End If
        rs.MoveNext
    Loop
    rs.Close
    RecordDQResult BatchID, "PROVISION_EXCEEDS_EXPOSURE", total, failCount
End Sub

Private Sub CheckMissingDefaultDate(ByVal BatchID As Long)
    ' DefaultDate lives on tbl_Account (set once, not re-supplied every
    ' snapshot), so this checks new accounts introduced by this batch that
    ' have no DefaultDate yet — they cannot be aged, have collateral
    ' consumed, or be screened for 3yr/6yr rules until this is fixed.
    RunCheck BatchID, "MISSING_DEFAULT_DATE", _
        "SELECT s.StagingID, s.AccountNumber FROM stg_AccountSnapshot s " & _
        "LEFT JOIN tbl_Account a ON a.AccountNumber = s.AccountNumber " & _
        "WHERE s.BatchID = " & BatchID & " AND (a.AccountID IS NULL OR a.DefaultDate IS NULL)", _
        "Account has no DefaultDate on tbl_Account; aging/collateral/off-balance engines cannot run for it."
End Sub

Private Sub CheckMissingClassification(ByVal BatchID As Long)
    RunCheck BatchID, "MISSING_CLASSIFICATION", _
        "SELECT StagingID, AccountNumber FROM stg_AccountSnapshot " & _
        "WHERE BatchID = " & BatchID & " AND (ClassificationStatus IS NULL OR ClassificationStatus = '')", _
        "Missing regulatory classification status."
End Sub

' ----------------------------------------------------------------------
' Shared helpers
' ----------------------------------------------------------------------
Private Sub RunCheck(ByVal BatchID As Long, ByVal CheckCode As String, ByVal sql As String, ByVal Reason As String)
    Dim db As DAO.Database, rs As DAO.Recordset
    Dim total As Long, failCount As Long

    Set db = CurrentDb
    total = DCount("*", "stg_AccountSnapshot", "BatchID = " & BatchID)
    Set rs = db.OpenRecordset(sql)
    Do While Not rs.EOF
        failCount = failCount + 1
        FlagInvalid rs!StagingID, CheckCode, Reason
        rs.MoveNext
    Loop
    rs.Close
    RecordDQResult BatchID, CheckCode, total, failCount
End Sub

Private Sub FlagInvalid(ByVal StagingID As Long, ByVal ErrorType As String, ByVal Description As String)
    CurrentDb.Execute "UPDATE stg_AccountSnapshot SET ValidationStatus = 'Invalid' WHERE StagingID = " & StagingID
    CurrentDb.Execute "INSERT INTO tbl_ImportError (ImportFileID, RowNumber, ErrorType, ErrorDescription) " & _
        "SELECT BatchID, RowNumber, '" & ErrorType & "', '" & Replace(Description, "'", "''") & _
        "' FROM stg_AccountSnapshot WHERE StagingID = " & StagingID
End Sub

Private Sub RecordDQResult(ByVal BatchID As Long, ByVal CheckCode As String, ByVal RowsChecked As Long, ByVal RowsFailed As Long)
    CurrentDb.Execute "INSERT INTO tbl_DataQuality (BatchID, CheckCode, RowsChecked, RowsFailed) VALUES (" & _
        BatchID & ", '" & CheckCode & "', " & RowsChecked & ", " & RowsFailed & ")"
End Sub
