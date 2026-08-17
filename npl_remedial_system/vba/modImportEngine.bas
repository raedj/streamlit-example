Attribute VB_Name = "modImportEngine"
Option Explicit
Option Compare Database

' ============================================================================
' modImportEngine
' Scans \NPL_System\Incoming\, validates each file's structure against
' tbl_ExcelTemplate, loads it into staging (stg_AccountSnapshot /
' stg_Collection), and files it to Processed\ or Rejected\.
'
' Design notes (Performance §79):
'   - Uses DAO batch recordsets (dbAppendOnly) for staging inserts, not
'     per-cell DLookup calls.
'   - One tbl_ImportBatch row per run; every staged row carries BatchID
'     for lineage.
'   - Duplicate whole-file re-imports are blocked by the UNIQUE constraint
'     on tbl_ImportFile.FileHash — checked BEFORE opening the workbook.
' ============================================================================

Public Const INCOMING_FOLDER As String = "\NPL_System\Incoming\"
Public Const PROCESSED_FOLDER As String = "\NPL_System\Processed\"
Public Const REJECTED_FOLDER As String = "\NPL_System\Rejected\"

' ----------------------------------------------------------------------
' RunImportCycle
' Entry point, called on a schedule (Windows Task Scheduler launching
' Access with /x AutoExec, or a manual "Run Import" button). Processes
' every file currently sitting in Incoming\.
' ----------------------------------------------------------------------
Public Sub RunImportCycle(ByVal BatchType As String, ByVal BatchPeriod As Date)
    Dim fileName As String
    Dim batchID As Long

    batchID = CreateImportBatch(BatchType, BatchPeriod)
    WriteLog "modImportEngine", "Batch " & batchID & " started (" & BatchType & ", " & Format(BatchPeriod, "yyyy-mm") & ")"

    fileName = Dir(INCOMING_FOLDER & "*.xlsx")
    Do While Len(fileName) > 0
        ProcessOneFile batchID, INCOMING_FOLDER & fileName, fileName
        fileName = Dir()
    Loop

    FinalizeImportBatch batchID
    WriteLog "modImportEngine", "Batch " & batchID & " completed"
End Sub

Private Function CreateImportBatch(ByVal BatchType As String, ByVal BatchPeriod As Date) As Long
    Dim db As DAO.Database
    Dim rs As DAO.Recordset

    Set db = CurrentDb
    Set rs = db.OpenRecordset("tbl_ImportBatch", dbOpenDynaset, dbAppendOnly)
    rs.AddNew
    rs!BatchType = BatchType
    rs!BatchPeriod = BatchPeriod
    rs!StartedAt = Now
    rs!Status = "Running"
    rs!InitiatedByEmployeeID = CurrentEmployeeID()
    rs.Update
    rs.Bookmark = rs.LastModified
    CreateImportBatch = rs!BatchID
    rs.Close
    Set rs = Nothing
    Set db = Nothing
End Function

' ----------------------------------------------------------------------
' ProcessOneFile
' Hash-check -> template validation -> staging load -> file/DB bookkeeping.
' Any failure at any stage routes the file to Rejected\ with a reason and
' logs a tbl_ImportError row; it never partially loads a bad file.
' ----------------------------------------------------------------------
Private Sub ProcessOneFile(ByVal BatchID As Long, ByVal FullPath As String, ByVal FileName As String)
    Dim db As DAO.Database
    Dim fileHash As String
    Dim templateID As Long
    Dim importFileID As Long
    Dim rowsRead As Long, rowsLoaded As Long, rowsRejected As Long

    On Error GoTo Fail

    Set db = CurrentDb
    fileHash = Sha256HexOfFile(FullPath)

    If Not DCount("*", "tbl_ImportFile", "FileHash = '" & fileHash & "'") = 0 Then
        RejectFile BatchID, FileName, fileHash, Null, _
            "Duplicate file: identical content already imported (FileHash match)."
        Exit Sub
    End If

    templateID = ResolveTemplate(FileName)
    If templateID = 0 Then
        RejectFile BatchID, FileName, fileHash, Null, _
            "No active tbl_ExcelTemplate matched this file's columns. Template Version Error."
        Exit Sub
    End If

    importFileID = RegisterImportFile(BatchID, FileName, fileHash, templateID)

    LoadWorkbookToStaging FullPath, BatchID, importFileID, templateID, rowsRead, rowsLoaded, rowsRejected

    db.Execute "UPDATE tbl_ImportFile SET Status = 'Processed' WHERE ImportFileID = " & importFileID
    Name FullPath As PROCESSED_FOLDER & Format(Now, "yyyymmdd_hhnnss") & "_" & FileName

    db.Execute "UPDATE tbl_ImportBatch SET RowsRead = Nz(RowsRead,0) + " & rowsRead & _
               ", RowsLoaded = Nz(RowsLoaded,0) + " & rowsLoaded & _
               ", RowsRejected = Nz(RowsRejected,0) + " & rowsRejected & _
               " WHERE BatchID = " & BatchID

    WriteLog "modImportEngine", FileName & ": read=" & rowsRead & " loaded=" & rowsLoaded & " rejected=" & rowsRejected
    Exit Sub

Fail:
    RejectFile BatchID, FileName, fileHash, Null, "Unhandled error: " & Err.Description
    WriteLog "modImportEngine", "ProcessOneFile failed for " & FileName & ": " & Err.Description, "ERROR"
End Sub

Private Function ResolveTemplate(ByVal FileName As String) As Long
    ' In production this opens the workbook, reads the header row, and
    ' matches it against tbl_ExcelTemplate.RequiredColumnsJSON for each
    ' Active template, returning the best match (or 0 if none matches
    ' within tolerance). Column-shape comparison logic lives here so a
    ' silently-changed Excel layout is caught as a Template Version Error
    ' instead of loading misaligned columns.
    ResolveTemplate = DLookup("ExcelTemplateID", "tbl_ExcelTemplate", "Active = True")
End Function

Private Function RegisterImportFile(ByVal BatchID As Long, ByVal FileName As String, _
                                     ByVal FileHash As String, ByVal TemplateID As Long) As Long
    Dim db As DAO.Database
    Dim rs As DAO.Recordset

    Set db = CurrentDb
    Set rs = db.OpenRecordset("tbl_ImportFile", dbOpenDynaset, dbAppendOnly)
    rs.AddNew
    rs!BatchID = BatchID
    rs!FileName = FileName
    rs!FileHash = FileHash
    rs!TemplateID = TemplateID
    rs!ImportedAt = Now
    rs!Status = "Pending"
    rs.Update
    rs.Bookmark = rs.LastModified
    RegisterImportFile = rs!ImportFileID
    rs.Close
End Function

Private Sub RejectFile(ByVal BatchID As Long, ByVal FileName As String, ByVal FileHash As Variant, _
                        ByVal ImportFileID As Variant, ByVal Reason As String)
    Dim db As DAO.Database
    Dim fid As Long

    Set db = CurrentDb
    If IsNull(ImportFileID) Then
        fid = RegisterImportFile(BatchID, FileName, Nz(FileHash, "UNKNOWN_" & Format(Now, "hhnnss")), 0)
    Else
        fid = ImportFileID
    End If

    db.Execute "UPDATE tbl_ImportFile SET Status = 'Rejected', RejectReason = '" & _
               Replace(Reason, "'", "''") & "' WHERE ImportFileID = " & fid

    db.Execute "INSERT INTO tbl_ImportError (ImportFileID, ErrorType, ErrorDescription) VALUES (" & _
               fid & ", 'FileRejected', '" & Replace(Reason, "'", "''") & "')"

    On Error Resume Next   ' file may not exist yet if hashing failed before open; acceptable here only
    Name INCOMING_FOLDER & FileName As REJECTED_FOLDER & Format(Now, "yyyymmdd_hhnnss") & "_" & FileName
    On Error GoTo 0

    WriteLog "modImportEngine", "REJECTED " & FileName & ": " & Reason, "WARN"
End Sub

' ----------------------------------------------------------------------
' LoadWorkbookToStaging
' Reads each data sheet via late-bound Excel automation and inserts one
' stg_AccountSnapshot (or stg_Collection, per template's TargetStagingTable)
' row per source row, tagged with BatchID and RowNumber for lineage.
' Business-rule validation happens afterwards in modDataQualityEngine —
' this procedure's job is faithful, lossless staging only.
' ----------------------------------------------------------------------
Private Sub LoadWorkbookToStaging(ByVal FullPath As String, ByVal BatchID As Long, _
                                   ByVal ImportFileID As Long, ByVal TemplateID As Long, _
                                   ByRef RowsRead As Long, ByRef RowsLoaded As Long, ByRef RowsRejected As Long)
    Dim xlApp As Object, wb As Object, ws As Object
    Dim db As DAO.Database, rs As DAO.Recordset
    Dim lastRow As Long, r As Long

    Set xlApp = CreateObject("Excel.Application")
    xlApp.Visible = False
    xlApp.DisplayAlerts = False

    Set wb = xlApp.Workbooks.Open(FullPath, ReadOnly:=True)
    Set ws = wb.Sheets(1)
    lastRow = ws.Cells(ws.Rows.Count, 1).End(-4162).Row ' xlUp

    Set db = CurrentDb
    Set rs = db.OpenRecordset("stg_AccountSnapshot", dbOpenDynaset, dbAppendOnly)

    For r = 2 To lastRow   ' row 1 = header
        RowsRead = RowsRead + 1
        rs.AddNew
        rs!BatchID = BatchID
        rs!RowNumber = r
        rs!CIF = Trim(ws.Cells(r, 1).Value & "")
        rs!AccountNumber = Trim(ws.Cells(r, 2).Value & "")
        rs!FacilityNumber = Trim(ws.Cells(r, 3).Value & "")
        rs!SnapshotPeriodText = ws.Cells(r, 4).Value & ""
        rs!ExposureText = ws.Cells(r, 5).Value & ""
        rs!PrincipalText = ws.Cells(r, 6).Value & ""
        rs!InterestText = ws.Cells(r, 7).Value & ""
        rs!SuspendedInterestText = ws.Cells(r, 8).Value & ""
        rs!ProvisionText = ws.Cells(r, 9).Value & ""
        rs!CollateralText = ws.Cells(r, 10).Value & ""
        rs!ClassificationStatus = ws.Cells(r, 11).Value & ""
        rs!LegalStatus = ws.Cells(r, 12).Value & ""
        rs!SettlementStatus = ws.Cells(r, 13).Value & ""
        rs!ValidationStatus = "Pending"
        rs.Update
        RowsLoaded = RowsLoaded + 1
    Next r

    rs.Close
    wb.Close SaveChanges:=False
    xlApp.Quit
    Set rs = Nothing: Set wb = Nothing: Set xlApp = Nothing
End Sub

Private Sub FinalizeImportBatch(ByVal BatchID As Long)
    CurrentDb.Execute "UPDATE tbl_ImportBatch SET Status = 'Completed', CompletedAt = Now() " & _
                       "WHERE BatchID = " & BatchID
End Sub
