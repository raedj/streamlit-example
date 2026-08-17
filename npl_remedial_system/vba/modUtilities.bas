Attribute VB_Name = "modUtilities"
Option Explicit
Option Compare Database

' ============================================================================
' modUtilities
' Shared helpers: configuration parameter access, structured logging,
' current-user resolution, and the one sanctioned error-handling pattern
' used by every other engine module.
'
' No engine module should read a threshold, a folder path, or a "current
' user" value any other way than through the functions in this module —
' that is what keeps sensitive numbers out of hard-coded VBA literals.
' ============================================================================

Public Const LOG_FOLDER As String = "\NPL_System\Logs\"

' ----------------------------------------------------------------------
' GetSystemParameter
' Resolves a configuration value from tbl_SystemParameter as of a given
' date (defaults to today). Raises an error rather than silently
' returning a default, so a missing parameter fails loudly at the point
' of use instead of corrupting a downstream calculation.
' ----------------------------------------------------------------------
Public Function GetSystemParameter(ByVal ParameterCode As String, _
                                    Optional ByVal AsOfDate As Variant) As Variant
    Dim db As DAO.Database
    Dim rs As DAO.Recordset
    Dim sql As String
    Dim effDate As Date

    If IsMissing(AsOfDate) Or IsNull(AsOfDate) Then
        effDate = Date
    Else
        effDate = CDate(AsOfDate)
    End If

    Set db = CurrentDb
    sql = "SELECT TOP 1 ParameterValue, DataType FROM tbl_SystemParameter " & _
          "WHERE ParameterCode = @Code " & _
          "AND EffectiveDate <= @AsOf " & _
          "AND (ExpiryDate IS NULL OR ExpiryDate > @AsOf) " & _
          "ORDER BY EffectiveDate DESC"

    Dim qd As DAO.QueryDef
    Set qd = db.CreateQueryDef("")
    qd.sql = sql
    qd.Parameters("@Code") = ParameterCode
    qd.Parameters("@AsOf") = effDate
    Set rs = qd.OpenRecordset()

    If rs.EOF Then
        Err.Raise vbObjectError + 5001, "modUtilities.GetSystemParameter", _
            "System parameter '" & ParameterCode & "' has no value effective " & Format(effDate, "yyyy-mm-dd") & _
            ". Add a row to tbl_SystemParameter before running this engine."
    End If

    Select Case rs!DataType
        Case "Integer"
            GetSystemParameter = CLng(rs!ParameterValue)
        Case "Currency"
            GetSystemParameter = CCur(rs!ParameterValue)
        Case "Double"
            GetSystemParameter = CDbl(rs!ParameterValue)
        Case "Date"
            GetSystemParameter = CDate(rs!ParameterValue)
        Case "Boolean"
            GetSystemParameter = CBool(rs!ParameterValue)
        Case Else
            GetSystemParameter = CStr(rs!ParameterValue)
    End Select

    rs.Close
    Set rs = Nothing
    Set qd = Nothing
    Set db = Nothing
End Function

' ----------------------------------------------------------------------
' CurrentEmployeeID
' Resolves the logged-in Windows/AD login (Environ / CurrentUser) to
' tbl_Employee.EmployeeID. Every engine that writes an audit row or an
' approval request calls this rather than trusting a form-level variable.
' ----------------------------------------------------------------------
Public Function CurrentEmployeeID() As Long
    Dim db As DAO.Database
    Dim rs As DAO.Recordset
    Dim loginName As String

    loginName = Environ("USERNAME")
    Set db = CurrentDb
    Set rs = db.OpenRecordset( _
        "SELECT EmployeeID FROM tbl_Employee WHERE WindowsLogin = '" & _
        Replace(loginName, "'", "''") & "' AND Active = True", dbOpenSnapshot)

    If rs.EOF Then
        Err.Raise vbObjectError + 5002, "modUtilities.CurrentEmployeeID", _
            "No active tbl_Employee row is linked to Windows login '" & loginName & _
            "'. Ask an Admin to link this account before using the system."
    Else
        CurrentEmployeeID = rs!EmployeeID
    End If

    rs.Close
    Set rs = Nothing
    Set db = Nothing
End Function

' ----------------------------------------------------------------------
' WriteLog
' Appends one line to a daily log file under LOG_FOLDER. Used by the
' import/engine modules for run-level diagnostics; tbl_AuditTrail (see
' modAuditTrail) is the record of business events, this is the record of
' *technical* execution (start/stop, row counts, exceptions raised).
' ----------------------------------------------------------------------
Public Sub WriteLog(ByVal SourceModule As String, ByVal Message As String, _
                     Optional ByVal Severity As String = "INFO")
    Dim filePath As String
    Dim fileNum As Integer

    filePath = LOG_FOLDER & "NPL_" & Format(Date, "yyyymmdd") & ".log"
    fileNum = FreeFile

    On Error GoTo LogFallback
    Open filePath For Append As #fileNum
    Print #fileNum, Format(Now, "yyyy-mm-dd hh:nn:ss") & vbTab & Severity & _
                    vbTab & SourceModule & vbTab & Message
    Close #fileNum
    Exit Sub

LogFallback:
    ' If the log folder is unreachable (mapped drive down, permissions), do
    ' not let logging itself take down an engine run. Surface it once via
    ' Debug.Print so it is visible during development/testing.
    Debug.Print "WriteLog failed: " & Err.Description & " | " & SourceModule & " | " & Message
End Sub

' ----------------------------------------------------------------------
' RaiseEngineError
' Standard wrapper so every engine reports failures the same way: logged,
' then re-raised so the caller (batch loop / month-end orchestrator) can
' decide whether to stop or continue with the next record.
' ----------------------------------------------------------------------
Public Sub RaiseEngineError(ByVal SourceModule As String, ByVal ErrNumber As Long, _
                             ByVal ErrDescription As String)
    WriteLog SourceModule, "ERROR " & ErrNumber & ": " & ErrDescription, "ERROR"
    Err.Raise vbObjectError + ErrNumber, SourceModule, ErrDescription
End Sub

' ----------------------------------------------------------------------
' Sha256HexOfFile
' Used by modImportEngine to compute tbl_ImportFile.FileHash for
' duplicate-import detection. Wraps the Windows CryptoAPI via
' System.Security.Cryptography is not available from VBA directly, so this
' shells out to certutil (present on every managed Windows desktop) rather
' than hand-rolling SHA-256 in VBA.
' ----------------------------------------------------------------------
Public Function Sha256HexOfFile(ByVal FilePath As String) As String
    Dim shellCmd As String
    Dim tempOut As String
    Dim fileNum As Integer
    Dim line As String
    Dim result As String

    tempOut = LOG_FOLDER & "hash_" & Format(Now, "hhnnssMMYYYY") & ".tmp"
    shellCmd = "cmd /c certutil -hashfile """ & FilePath & """ SHA256 > """ & tempOut & """"
    Shell shellCmd, vbHide

    ' NOTE: in production, replace the fixed pause below with a WaitForSingleObject
    ' on the shelled process handle; a blind Sleep is a placeholder only.
    Dim i As Long
    For i = 1 To 20000000: Next i

    fileNum = FreeFile
    Open tempOut For Input As #fileNum
    Do While Not EOF(fileNum)
        Line Input #fileNum, line
        If Len(line) = 64 And line = LCase(line) Then
            result = line
            Exit Do
        End If
    Loop
    Close #fileNum
    Kill tempOut

    Sha256HexOfFile = result
End Function
