-- =====================================================================
-- Remedial & NPL Portfolio Management System — Database Schema
-- Dialect: Microsoft Access (Jet/ACE SQL). Run each CREATE TABLE
-- statement individually (Jet does not support multi-statement scripts) —
-- e.g. one query object per statement, or via VBA CurrentDb.Execute in a
-- loop. Portable subset used throughout (see 08_DEPLOYMENT_GUIDE.md for
-- the SQL Server migration mapping: AUTOINCREMENT -> IDENTITY,
-- CURRENCY -> DECIMAL(19,4), YESNO -> BIT, TEXT(n) -> NVARCHAR(n)).
--
-- Naming: tbl_ prefix for permanent tables, stg_ for staging,
-- PK = <Table>ID, FK = <ReferencedTable>ID.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. REFERENCE / ORGANIZATION
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Branch (
    BranchID        AUTOINCREMENT PRIMARY KEY,
    BranchCode      TEXT(20)  NOT NULL,
    BranchName      TEXT(100) NOT NULL,
    Region          TEXT(50),
    Active          YESNO NOT NULL DEFAULT True,
    CONSTRAINT UQ_Branch_Code UNIQUE (BranchCode)
);

CREATE TABLE tbl_Department (
    DepartmentID    AUTOINCREMENT PRIMARY KEY,
    DepartmentName  TEXT(100) NOT NULL,
    Active          YESNO NOT NULL DEFAULT True
);

CREATE TABLE tbl_Team (
    TeamID          AUTOINCREMENT PRIMARY KEY,
    DepartmentID    LONG NOT NULL,
    TeamName        TEXT(100) NOT NULL,
    TeamManagerEmployeeID LONG,
    Active          YESNO NOT NULL DEFAULT True,
    CONSTRAINT FK_Team_Department FOREIGN KEY (DepartmentID) REFERENCES tbl_Department (DepartmentID)
);

CREATE TABLE tbl_Employee (
    EmployeeID      AUTOINCREMENT PRIMARY KEY,
    EmployeeCode    TEXT(20) NOT NULL,
    FullName        TEXT(150) NOT NULL,
    RoleCode        TEXT(30) NOT NULL,   -- Employee / TeamManager / DeptManager / Admin / Audit
    TeamID          LONG,
    BranchID        LONG,
    ManagerEmployeeID LONG,              -- direct manager, for segregation-of-duties checks
    WindowsLogin    TEXT(100),           -- maps to Access CurrentUser / AD login
    Active          YESNO NOT NULL DEFAULT True,
    HireDate        DATETIME,
    TerminationDate DATETIME,
    CONSTRAINT UQ_Employee_Code UNIQUE (EmployeeCode),
    CONSTRAINT FK_Employee_Team FOREIGN KEY (TeamID) REFERENCES tbl_Team (TeamID),
    CONSTRAINT FK_Employee_Branch FOREIGN KEY (BranchID) REFERENCES tbl_Branch (BranchID),
    CONSTRAINT FK_Employee_Manager FOREIGN KEY (ManagerEmployeeID) REFERENCES tbl_Employee (EmployeeID)
);

CREATE TABLE tbl_ExternalOffice (
    ExternalOfficeID AUTOINCREMENT PRIMARY KEY,
    OfficeName      TEXT(150) NOT NULL,
    OfficeType      TEXT(30)  NOT NULL,  -- Collection Agency / Law Office / Valuer / Auctioneer
    CommissionRateDefault DOUBLE,
    ContractStartDate DATETIME,
    ContractEndDate   DATETIME,
    Active          YESNO NOT NULL DEFAULT True
);

CREATE TABLE tbl_UserRole (
    UserRoleID      AUTOINCREMENT PRIMARY KEY,
    RoleCode        TEXT(30) NOT NULL,
    RoleName        TEXT(100) NOT NULL,
    CanApprove      YESNO NOT NULL DEFAULT False,
    CanImport       YESNO NOT NULL DEFAULT False,
    CanConfigure    YESNO NOT NULL DEFAULT False,
    ReadOnly        YESNO NOT NULL DEFAULT False,
    CONSTRAINT UQ_UserRole_Code UNIQUE (RoleCode)
);

-- ---------------------------------------------------------------------
-- 2. CONFIGURATION (never hard-code these in VBA)
-- ---------------------------------------------------------------------

CREATE TABLE tbl_SystemParameter (
    SystemParameterID    AUTOINCREMENT PRIMARY KEY,
    ParameterCode           TEXT(50) NOT NULL,   -- e.g. COLLATERAL_CONSUMPTION_MONTHS
    ParameterValue             TEXT(200) NOT NULL,
    DataType                     TEXT(20) NOT NULL, -- Integer/Currency/Double/Text/Date/Boolean
    Description                    MEMO,
    EffectiveDate                    DATETIME NOT NULL,
    ExpiryDate                        DATETIME,
    CONSTRAINT UQ_Param_Code_Effective UNIQUE (ParameterCode, EffectiveDate)
);

CREATE TABLE tbl_RegulatoryRules (
    RegulatoryRuleID      AUTOINCREMENT PRIMARY KEY,
    RuleCode                 TEXT(50) NOT NULL,   -- e.g. OFFBALANCE_3YEAR, OFFBALANCE_6YEAR
    RuleName                   TEXT(150) NOT NULL,
    RuleType                     TEXT(30) NOT NULL, -- Threshold/Classification/Provisioning/Disclosure
    ThresholdValue                 DOUBLE,
    ThresholdUnit                    TEXT(20),       -- Years/Months/Percent/Amount
    EffectiveDate                      DATETIME NOT NULL,
    ExpiryDate                           DATETIME,
    ApplicablePortfolio                    TEXT(50),
    CalculationMethod                        MEMO,
    RequiredAction                             MEMO,
    ApprovalLevel                                TEXT(20),
    RegulatorySource                               TEXT(100), -- e.g. "CBJ Circular X/2024"
    Active                                           YESNO NOT NULL DEFAULT True,
    CONSTRAINT UQ_RegRule_Code_Effective UNIQUE (RuleCode, EffectiveDate)
);

CREATE TABLE tbl_ExcelTemplate (
    ExcelTemplateID       AUTOINCREMENT PRIMARY KEY,
    TemplateName             TEXT(100) NOT NULL,
    TemplateVersion             TEXT(20) NOT NULL,
    RequiredColumnsJSON           MEMO NOT NULL,  -- column name/type/required flag, as JSON text
    Active                          YESNO NOT NULL DEFAULT True,
    CONSTRAINT UQ_Template_Name_Version UNIQUE (TemplateName, TemplateVersion)
);

CREATE TABLE tbl_Target (
    TargetID              AUTOINCREMENT PRIMARY KEY,
    TargetPeriod              DATETIME NOT NULL,
    Scope                       TEXT(30) NOT NULL,  -- Bank/Department/Team/Employee/Office/Product
    ScopeID                       LONG,
    TargetType                     TEXT(30) NOT NULL, -- Recovery/NetRecovery/CostPerJOD
    TargetValue                     CURRENCY NOT NULL
);

CREATE TABLE tbl_Forecast (
    ForecastID             AUTOINCREMENT PRIMARY KEY,
    ForecastPeriod             DATETIME NOT NULL,
    Scope                        TEXT(30) NOT NULL,
    ScopeID                        LONG,
    ForecastType                    TEXT(30) NOT NULL,
    ForecastValue                     CURRENCY NOT NULL,
    ModelVersion                       TEXT(20)
);

CREATE TABLE tbl_KPIResult (
    KPIResultID            AUTOINCREMENT PRIMARY KEY,
    KPIPeriod                  DATETIME NOT NULL,
    KPICode                      TEXT(50) NOT NULL,
    Scope                          TEXT(30) NOT NULL,
    ScopeID                          LONG,
    KPIValue                          DOUBLE NOT NULL,
    CalculatedAt                        DATETIME NOT NULL DEFAULT Now()
);
CREATE INDEX IX_KPIResult_PeriodCode ON tbl_KPIResult (KPIPeriod, KPICode);

CREATE TABLE tbl_DashboardParameter (
    DashboardParameterID   AUTOINCREMENT PRIMARY KEY,
    DashboardCode              TEXT(50) NOT NULL,
    ParameterKey                  TEXT(50) NOT NULL,
    ParameterValue                  TEXT(200),
    CONSTRAINT UQ_DashParam UNIQUE (DashboardCode, ParameterKey)
);

-- ---------------------------------------------------------------------
-- 3. IMPORT / DATA QUALITY
-- ---------------------------------------------------------------------

CREATE TABLE tbl_ImportBatch (
    BatchID                AUTOINCREMENT PRIMARY KEY,
    BatchType                  TEXT(20) NOT NULL,   -- Daily/Weekly/MonthlySnapshot/HistoricalReload
    BatchPeriod                  DATETIME NOT NULL,
    StartedAt                      DATETIME NOT NULL DEFAULT Now(),
    CompletedAt                      DATETIME,
    Status                             TEXT(20) NOT NULL DEFAULT 'Running', -- Running/Completed/Failed/RolledBack
    InitiatedByEmployeeID                LONG,
    RowsRead                               LONG,
    RowsLoaded                               LONG,
    RowsRejected                               LONG,
    DataQualityScore                             DOUBLE
);

CREATE TABLE tbl_ImportFile (
    ImportFileID           AUTOINCREMENT PRIMARY KEY,
    BatchID                    LONG NOT NULL,
    FileName                     TEXT(255) NOT NULL,
    FileHash                       TEXT(64) NOT NULL,  -- SHA-256 of file content, blocks re-import of identical file
    TemplateID                       LONG,
    ImportedAt                         DATETIME NOT NULL DEFAULT Now(),
    Status                               TEXT(20) NOT NULL DEFAULT 'Pending', -- Pending/Processed/Rejected
    RejectReason                          MEMO,
    CONSTRAINT UQ_ImportFile_Hash UNIQUE (FileHash),
    CONSTRAINT FK_ImportFile_Batch FOREIGN KEY (BatchID) REFERENCES tbl_ImportBatch (BatchID),
    CONSTRAINT FK_ImportFile_Template FOREIGN KEY (TemplateID) REFERENCES tbl_ExcelTemplate (ExcelTemplateID)
);

CREATE TABLE tbl_ImportError (
    ImportErrorID           AUTOINCREMENT PRIMARY KEY,
    ImportFileID                LONG NOT NULL,
    RowNumber                     LONG,
    ColumnName                      TEXT(100),
    ErrorType                         TEXT(50) NOT NULL,
    ErrorDescription                    MEMO,
    RawValue                              TEXT(255),
    CONSTRAINT FK_ImportError_File FOREIGN KEY (ImportFileID) REFERENCES tbl_ImportFile (ImportFileID)
);

CREATE TABLE tbl_DataQuality (
    DataQualityID            AUTOINCREMENT PRIMARY KEY,
    BatchID                      LONG NOT NULL,
    CheckCode                      TEXT(50) NOT NULL,
    RowsChecked                      LONG,
    RowsFailed                         LONG,
    CheckedAt                            DATETIME NOT NULL DEFAULT Now(),
    CONSTRAINT FK_DQ_Batch FOREIGN KEY (BatchID) REFERENCES tbl_ImportBatch (BatchID)
);

-- Staging tables mirror the raw import shape before validation; kept
-- deliberately loose (mostly TEXT) so bad source data doesn't blow up
-- the load — modDataQualityEngine converts/validates on the way into
-- tbl_MonthlySnapshot / tbl_Collection etc.
CREATE TABLE stg_AccountSnapshot (
    StagingID       AUTOINCREMENT PRIMARY KEY,
    BatchID         LONG NOT NULL,
    RowNumber       LONG,
    CIF             TEXT(30),
    AccountNumber   TEXT(40),
    FacilityNumber  TEXT(40),
    SnapshotPeriodText TEXT(20),
    ExposureText    TEXT(30),
    PrincipalText   TEXT(30),
    InterestText    TEXT(30),
    SuspendedInterestText TEXT(30),
    ProvisionText   TEXT(30),
    CollateralText  TEXT(30),
    ClassificationStatus TEXT(30),
    LegalStatus     TEXT(30),
    SettlementStatus TEXT(30),
    RawRowJSON      MEMO,
    ValidationStatus TEXT(20) DEFAULT 'Pending', -- Pending/Valid/Invalid
    CONSTRAINT FK_StgSnap_Batch FOREIGN KEY (BatchID) REFERENCES tbl_ImportBatch (BatchID)
);

CREATE TABLE stg_Collection (
    StagingID       AUTOINCREMENT PRIMARY KEY,
    BatchID         LONG NOT NULL,
    RowNumber       LONG,
    AccountNumber   TEXT(40),
    CollectionDateText TEXT(20),
    CollectionType   TEXT(40),
    AmountText       TEXT(30),
    RawRowJSON       MEMO,
    ValidationStatus  TEXT(20) DEFAULT 'Pending',
    CONSTRAINT FK_StgColl_Batch FOREIGN KEY (BatchID) REFERENCES tbl_ImportBatch (BatchID)
);
-- ---------------------------------------------------------------------
-- 4. CUSTOMER / ACCOUNT MASTER
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Customer (
    CustomerID      AUTOINCREMENT PRIMARY KEY,
    CIF             TEXT(30) NOT NULL,
    CustomerName    TEXT(200) NOT NULL,
    CustomerReference TEXT(50),          -- masked/tokenised National ID per bank policy
    Segment         TEXT(50),            -- Retail / SME / Corporate ...
    Sector          TEXT(50),
    GroupID         LONG,                -- self-referencing customer group / related parties
    CONSTRAINT UQ_Customer_CIF UNIQUE (CIF)
);

CREATE TABLE tbl_Facility (
    FacilityID      AUTOINCREMENT PRIMARY KEY,
    FacilityNumber  TEXT(40) NOT NULL,
    CustomerID      LONG NOT NULL,
    Product         TEXT(50) NOT NULL,   -- Personal/CC/Auto/Mortgage/SME/Corporate/Overdraft/Other
    OriginalAmount  CURRENCY NOT NULL,
    OriginationDate DATETIME,
    CONSTRAINT UQ_Facility_Number UNIQUE (FacilityNumber),
    CONSTRAINT FK_Facility_Customer FOREIGN KEY (CustomerID) REFERENCES tbl_Customer (CustomerID)
);

CREATE TABLE tbl_Account (
    AccountID           AUTOINCREMENT PRIMARY KEY,
    AccountNumber       TEXT(40) NOT NULL,
    FacilityID          LONG NOT NULL,
    CustomerID          LONG NOT NULL,
    BranchID            LONG,
    RelationshipManagerEmployeeID LONG,
    AssignedEmployeeID  LONG,            -- current remedial officer (see tbl_Assignment for history)
    AssignedExternalOfficeID LONG,

    -- === Lifecycle dates — never conflate these ===
    DefaultDate         DATETIME,        -- anchor for ALL aging/consumption/3yr/6yr clocks
    NPLDate             DATETIME,        -- classification date per policy
    SystemEntryDate     DATETIME NOT NULL, -- date the record entered THIS system only
    OffBalanceDate      DATETIME,
    WriteOffDate        DATETIME,
    SettlementDate       DATETIME,
    ClosureDate         DATETIME,

    -- === Current position (also carried per-period in tbl_MonthlySnapshot) ===
    CurrentExposure     CURRENCY,
    PrincipalBalance    CURRENCY,
    InterestBalance     CURRENCY,
    SuspendedInterestBalance CURRENCY,
    PenaltiesBalance    CURRENCY,
    ProvisionBalance    CURRENCY,

    ClassificationStatus TEXT(30),        -- per tbl_RegulatoryRules classification scheme
    LegalStatus         TEXT(30) DEFAULT 'None',
    SettlementStatus    TEXT(30) DEFAULT 'None',
    WriteOffStatus      TEXT(30) DEFAULT 'None',
    OffBalanceStatus    TEXT(30) DEFAULT 'On-Balance',
    AccountLifecycleStage TEXT(30) DEFAULT 'NPL', -- NPL/Remedial/Settlement/Legal/WriteOff/OffBalance/Recovery/Closed

    Active              YESNO NOT NULL DEFAULT True,
    CreatedAt           DATETIME NOT NULL DEFAULT Now(),
    CreatedByEmployeeID LONG,

    CONSTRAINT UQ_Account_Number UNIQUE (AccountNumber),
    CONSTRAINT FK_Account_Facility FOREIGN KEY (FacilityID) REFERENCES tbl_Facility (FacilityID),
    CONSTRAINT FK_Account_Customer FOREIGN KEY (CustomerID) REFERENCES tbl_Customer (CustomerID),
    CONSTRAINT FK_Account_Branch FOREIGN KEY (BranchID) REFERENCES tbl_Branch (BranchID),
    CONSTRAINT FK_Account_AssignedEmp FOREIGN KEY (AssignedEmployeeID) REFERENCES tbl_Employee (EmployeeID),
    CONSTRAINT FK_Account_AssignedOffice FOREIGN KEY (AssignedExternalOfficeID) REFERENCES tbl_ExternalOffice (ExternalOfficeID)
);
CREATE INDEX IX_Account_DefaultDate ON tbl_Account (DefaultDate);
CREATE INDEX IX_Account_Customer ON tbl_Account (CustomerID);
CREATE INDEX IX_Account_Stage ON tbl_Account (AccountLifecycleStage);

CREATE TABLE tbl_Assignment (
    AssignmentID    AUTOINCREMENT PRIMARY KEY,
    AccountID       LONG NOT NULL,
    EmployeeID      LONG,
    ExternalOfficeID LONG,
    AssignedDate    DATETIME NOT NULL,
    UnassignedDate  DATETIME,
    Reason          TEXT(200),
    CONSTRAINT FK_Assignment_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID),
    CONSTRAINT FK_Assignment_Employee FOREIGN KEY (EmployeeID) REFERENCES tbl_Employee (EmployeeID),
    CONSTRAINT FK_Assignment_Office FOREIGN KEY (ExternalOfficeID) REFERENCES tbl_ExternalOffice (ExternalOfficeID)
);
CREATE INDEX IX_Assignment_Account ON tbl_Assignment (AccountID);

-- ---------------------------------------------------------------------
-- 5. MONTHLY SNAPSHOT (append-only history — never overwritten)
-- ---------------------------------------------------------------------

CREATE TABLE tbl_MonthlySnapshot (
    SnapshotID          AUTOINCREMENT PRIMARY KEY,
    AccountID           LONG NOT NULL,
    SnapshotPeriod       DATETIME NOT NULL,  -- always last day of month
    BatchID             LONG NOT NULL,       -- lineage: which import produced this row

    Exposure            CURRENCY,
    Principal           CURRENCY,
    Interest            CURRENCY,
    SuspendedInterest   CURRENCY,
    Provision           CURRENCY,
    CollateralValue     CURRENCY,

    ClassificationStatus TEXT(30),
    LegalStatus         TEXT(30),
    SettlementStatus    TEXT(30),
    OffBalanceStatus    TEXT(30),
    WriteOffStatus      TEXT(30),

    CreatedAt           DATETIME NOT NULL DEFAULT Now(),
    CONSTRAINT UQ_Snapshot_Account_Period UNIQUE (AccountID, SnapshotPeriod),
    CONSTRAINT FK_Snapshot_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID),
    CONSTRAINT FK_Snapshot_Batch FOREIGN KEY (BatchID) REFERENCES tbl_ImportBatch (BatchID)
);
CREATE INDEX IX_Snapshot_Period ON tbl_MonthlySnapshot (SnapshotPeriod);
CREATE INDEX IX_Snapshot_Account ON tbl_MonthlySnapshot (AccountID);

-- ---------------------------------------------------------------------
-- 6. MOVEMENT ENGINE
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Movement (
    MovementID          AUTOINCREMENT PRIMARY KEY,
    AccountID           LONG NOT NULL,
    MovementPeriod       DATETIME NOT NULL,
    MovementType         TEXT(30) NOT NULL,  -- Opening/NewMovement/Collection/Waiver/WriteOff/OffBalance/OtherReduction/Closing
    MovementCategory     TEXT(20) NOT NULL,  -- Portfolio / Recovery / Financial  (see 04_ENGINES_SPEC)
    Amount               CURRENCY NOT NULL,
    IsRecovery           YESNO NOT NULL DEFAULT False,
    SourceReferenceID    LONG,               -- points to tbl_Collection/tbl_Waiver/tbl_WriteOff/tbl_OffBalance row
    BatchID              LONG,
    CreatedAt            DATETIME NOT NULL DEFAULT Now(),
    CONSTRAINT FK_Movement_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);
CREATE INDEX IX_Movement_AccountPeriod ON tbl_Movement (AccountID, MovementPeriod);

CREATE TABLE tbl_MovementException (
    MovementExceptionID AUTOINCREMENT PRIMARY KEY,
    AccountID           LONG NOT NULL,
    MovementPeriod       DATETIME NOT NULL,
    ExpectedClosing      CURRENCY,
    ActualClosing        CURRENCY,
    Difference           CURRENCY,
    ExceptionType         TEXT(30) DEFAULT 'Unexplained Movement',
    Status                TEXT(20) DEFAULT 'Open',  -- Open/UnderReview/Resolved
    ResolutionNote        MEMO,
    ResolvedByEmployeeID  LONG,
    ResolvedDate          DATETIME,
    CONSTRAINT FK_MovExc_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

-- ---------------------------------------------------------------------
-- 7. COLLECTIONS / RECOVERY
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Collection (
    CollectionID        AUTOINCREMENT PRIMARY KEY,
    AccountID           LONG NOT NULL,
    CollectionDate       DATETIME NOT NULL,
    CollectionType        TEXT(40) NOT NULL, -- Cash/Auction-Vehicle/Auction-RealEstate/VoluntaryTransfer/
                                              -- CollateralSale/Insurance/Guarantor/Legal/Settlement/Rescheduling/Other
    RecoveryClassification TEXT(30) NOT NULL, -- Cash/Collateral/Legal/Settlement/Insurance/Auction/VoluntaryTransfer/Guarantor/Other
    GrossAmount           CURRENCY NOT NULL,
    IsRecovery             YESNO NOT NULL DEFAULT True,   -- False for same-month Off-Balance-only movements
    Source                 TEXT(10) NOT NULL,             -- Import / Manual
    EntrySource            TEXT(20) DEFAULT 'Import',      -- mirrors Source; kept for report readability
    EnteredByEmployeeID    LONG,
    ApprovalStatus          TEXT(20) NOT NULL DEFAULT 'Approved', -- Manual entries start 'Draft'
    ApprovalID              LONG,        -- FK to tbl_Approval when manual
    BatchID                 LONG,
    CreatedAt               DATETIME NOT NULL DEFAULT Now(),
    CONSTRAINT FK_Collection_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);
CREATE INDEX IX_Collection_AccountDate ON tbl_Collection (AccountID, CollectionDate);

CREATE TABLE tbl_CollectionAllocation (
    CollectionAllocationID AUTOINCREMENT PRIMARY KEY,
    CollectionID            LONG NOT NULL,
    WaterfallStep            TEXT(30) NOT NULL,  -- Principal/Interest/SuspendedInterest/ProvisionImpact/
                                                  -- Waiver/CollateralImpact/Costs
    Amount                    CURRENCY NOT NULL,
    StepOrder                 INTEGER NOT NULL,
    CONSTRAINT FK_Alloc_Collection FOREIGN KEY (CollectionID) REFERENCES tbl_Collection (CollectionID)
);

CREATE TABLE tbl_RecoveryCost (
    RecoveryCostID       AUTOINCREMENT PRIMARY KEY,
    CollectionID          LONG NOT NULL,
    CostType               TEXT(30) NOT NULL, -- Legal/Auction/Commission/ExternalOffice/Valuation/Transfer/Other
    Amount                  CURRENCY NOT NULL,
    ExternalOfficeID        LONG,
    CONSTRAINT FK_Cost_Collection FOREIGN KEY (CollectionID) REFERENCES tbl_Collection (CollectionID),
    CONSTRAINT FK_Cost_Office FOREIGN KEY (ExternalOfficeID) REFERENCES tbl_ExternalOffice (ExternalOfficeID)
);

CREATE TABLE tbl_Waiver (
    WaiverID           AUTOINCREMENT PRIMARY KEY,
    AccountID          LONG NOT NULL,
    WaiverDate          DATETIME NOT NULL,
    WaiverAmount         CURRENCY NOT NULL,
    WaiverType            TEXT(30),
    Reason                MEMO,
    ApprovedByEmployeeID  LONG,
    ApprovalDate          DATETIME,
    ApprovalID            LONG,
    CONSTRAINT FK_Waiver_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

-- ---------------------------------------------------------------------
-- 8. SETTLEMENTS
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Settlement (
    SettlementID        AUTOINCREMENT PRIMARY KEY,
    AccountID           LONG NOT NULL,
    SettlementDate        DATETIME NOT NULL,
    SettlementAmount       CURRENCY NOT NULL,
    WaiverAmount            CURRENCY DEFAULT 0,
    ExpectedRecovery         CURRENCY,
    InstallmentCount          INTEGER,
    Status                     TEXT(20) DEFAULT 'Active', -- Active/Completed/Broken
    BrokenDate                 DATETIME,
    ApprovalID                 LONG,
    CONSTRAINT FK_Settlement_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_SettlementPayment (
    SettlementPaymentID AUTOINCREMENT PRIMARY KEY,
    SettlementID          LONG NOT NULL,
    DueDate                 DATETIME NOT NULL,
    DueAmount                CURRENCY NOT NULL,
    PaidDate                 DATETIME,
    PaidAmount                CURRENCY,
    Status                     TEXT(20) DEFAULT 'Pending', -- Pending/Paid/Missed
    CONSTRAINT FK_SettlePmt_Settlement FOREIGN KEY (SettlementID) REFERENCES tbl_Settlement (SettlementID)
);

-- ---------------------------------------------------------------------
-- 9. LEGAL
-- ---------------------------------------------------------------------

CREATE TABLE tbl_LegalCase (
    LegalCaseID          AUTOINCREMENT PRIMARY KEY,
    AccountID            LONG NOT NULL,
    CaseNumber             TEXT(50) NOT NULL,
    Court                   TEXT(100),
    ClaimAmount              CURRENCY,
    OutstandingAmount        CURRENCY,
    LawyerName                TEXT(100),
    ExternalOfficeID          LONG,
    FilingDate                 DATETIME,
    JudgmentDate                DATETIME,
    ExecutionDate                DATETIME,
    CaseStatus                    TEXT(30) DEFAULT 'Open', -- Open/Judgment/Execution/Closed/Dismissed
    CONSTRAINT FK_LegalCase_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID),
    CONSTRAINT FK_LegalCase_Office FOREIGN KEY (ExternalOfficeID) REFERENCES tbl_ExternalOffice (ExternalOfficeID)
);

CREATE TABLE tbl_LegalAction (
    LegalActionID        AUTOINCREMENT PRIMARY KEY,
    LegalCaseID           LONG NOT NULL,
    ActionDate              DATETIME NOT NULL,
    ActionType                TEXT(50) NOT NULL,
    Notes                      MEMO,
    CONSTRAINT FK_LegalAction_Case FOREIGN KEY (LegalCaseID) REFERENCES tbl_LegalCase (LegalCaseID)
);

CREATE TABLE tbl_LegalNote (
    LegalNoteID           AUTOINCREMENT PRIMARY KEY,
    LegalCaseID            LONG NOT NULL,
    NoteDate                 DATETIME NOT NULL DEFAULT Now(),
    EmployeeID                LONG,
    NoteText                   MEMO,
    CONSTRAINT FK_LegalNote_Case FOREIGN KEY (LegalCaseID) REFERENCES tbl_LegalCase (LegalCaseID)
);

-- ---------------------------------------------------------------------
-- 10. COLLATERAL
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Collateral (
    CollateralID          AUTOINCREMENT PRIMARY KEY,
    AccountID              LONG NOT NULL,
    CollateralType            TEXT(40) NOT NULL,  -- RealEstate/Vehicle/Cash/Guarantee/Other
    OriginalValue              CURRENCY NOT NULL,
    OriginalValueDate           DATETIME NOT NULL,
    EligibleForConsumption       YESNO NOT NULL DEFAULT True,
    Active                        YESNO NOT NULL DEFAULT True,
    CONSTRAINT FK_Collateral_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_CollateralValuation (
    CollateralValuationID  AUTOINCREMENT PRIMARY KEY,
    CollateralID             LONG NOT NULL,
    ValuationDate              DATETIME NOT NULL,
    Value                       CURRENCY NOT NULL,
    ValuerName                  TEXT(100),
    CONSTRAINT FK_Valuation_Collateral FOREIGN KEY (CollateralID) REFERENCES tbl_Collateral (CollateralID)
);

CREATE TABLE tbl_CollateralConsumption (
    CollateralConsumptionID AUTOINCREMENT PRIMARY KEY,
    CollateralID              LONG NOT NULL,
    ConsumptionPeriod           DATETIME NOT NULL,  -- month this consumption row applies to
    MonthsSinceDefault           INTEGER NOT NULL,
    MonthlyConsumptionAmount      CURRENCY NOT NULL,
    ConsumedToDate                 CURRENCY NOT NULL,
    RemainingValue                  CURRENCY NOT NULL,
    CONSTRAINT UQ_Consumption_Collateral_Period UNIQUE (CollateralID, ConsumptionPeriod),
    CONSTRAINT FK_Consumption_Collateral FOREIGN KEY (CollateralID) REFERENCES tbl_Collateral (CollateralID)
);

-- ---------------------------------------------------------------------
-- 11. PROVISION / SUSPENDED INTEREST / OFF-BALANCE / WRITE-OFF
-- ---------------------------------------------------------------------

CREATE TABLE tbl_ProvisionSnapshot (
    ProvisionSnapshotID    AUTOINCREMENT PRIMARY KEY,
    AccountID                LONG NOT NULL,
    SnapshotPeriod             DATETIME NOT NULL,
    OpeningProvision            CURRENCY,
    ProvisionIncrease            CURRENCY,
    ProvisionDecrease             CURRENCY,
    ProvisionUtilization            CURRENCY,
    ProvisionRelease                 CURRENCY,
    ClosingProvision                  CURRENCY,
    ReleaseClassification              TEXT(10),   -- Profit / Saving, see modProvisionSuspendedInterestEngine
    CONSTRAINT UQ_Provision_Account_Period UNIQUE (AccountID, SnapshotPeriod),
    CONSTRAINT FK_Provision_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_SuspendedInterest (
    SuspendedInterestID     AUTOINCREMENT PRIMARY KEY,
    AccountID                 LONG NOT NULL,
    SnapshotPeriod              DATETIME NOT NULL,
    OpeningBalance                CURRENCY,
    NewSuspension                  CURRENCY,
    Recovery                        CURRENCY,
    Waiver                           CURRENCY,
    Reversal                         CURRENCY,
    ClosingBalance                    CURRENCY,
    ReversalClassification             TEXT(10),  -- Profit / Saving
    CONSTRAINT UQ_SI_Account_Period UNIQUE (AccountID, SnapshotPeriod),
    CONSTRAINT FK_SI_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_OffBalance (
    OffBalanceID            AUTOINCREMENT PRIMARY KEY,
    AccountID                 LONG NOT NULL,
    OffBalanceDate               DATETIME NOT NULL,
    ExposureAtOffBalance          CURRENCY,
    ProvisionAtOffBalance          CURRENCY,
    SuspendedInterestAtOffBalance   CURRENCY,
    ApprovalID                       LONG,
    Reason                            MEMO,
    CONSTRAINT FK_OffBalance_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_WriteOff (
    WriteOffID              AUTOINCREMENT PRIMARY KEY,
    AccountID                 LONG NOT NULL,
    WriteOffDate                 DATETIME NOT NULL,
    WriteOffAmount                 CURRENCY NOT NULL,
    ApprovalID                       LONG,
    Reason                            MEMO,
    CONSTRAINT FK_WriteOff_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_PostWriteOffRecovery (
    PostWriteOffRecoveryID   AUTOINCREMENT PRIMARY KEY,
    WriteOffID                 LONG NOT NULL,
    RecoveryDate                  DATETIME NOT NULL,
    Amount                          CURRENCY NOT NULL,
    CONSTRAINT FK_PWOR_WriteOff FOREIGN KEY (WriteOffID) REFERENCES tbl_WriteOff (WriteOffID)
);

-- ---------------------------------------------------------------------
-- 12. FOLLOW-UP / NOTES / PROMISES
-- ---------------------------------------------------------------------

CREATE TABLE tbl_AccountNote (
    AccountNoteID     AUTOINCREMENT PRIMARY KEY,
    AccountID           LONG NOT NULL,
    NoteDate               DATETIME NOT NULL DEFAULT Now(),
    EmployeeID              LONG,
    NoteText                 MEMO,
    CONSTRAINT FK_Note_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_FollowUp (
    FollowUpID         AUTOINCREMENT PRIMARY KEY,
    AccountID             LONG NOT NULL,
    FollowUpDate             DATETIME NOT NULL,
    ContactResult              TEXT(30),
    EmployeeID                  LONG,
    NextFollowUpDate              DATETIME,
    CONSTRAINT FK_FollowUp_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_Promise (
    PromiseID          AUTOINCREMENT PRIMARY KEY,
    AccountID             LONG NOT NULL,
    PromiseDate              DATETIME NOT NULL,
    PromisedAmount             CURRENCY,
    PromisedPayDate             DATETIME,
    Status                        TEXT(20) DEFAULT 'Open', -- Open/Kept/Broken
    CONSTRAINT FK_Promise_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

-- ---------------------------------------------------------------------
-- 13. APPROVAL WORKFLOW (maker-checker)
-- ---------------------------------------------------------------------

CREATE TABLE tbl_Approval (
    ApprovalID           AUTOINCREMENT PRIMARY KEY,
    EntityType              TEXT(30) NOT NULL,   -- Collection/Waiver/Settlement/WriteOff/OffBalance/Adjustment
    EntityID                  LONG NOT NULL,
    RequestedByEmployeeID       LONG NOT NULL,
    RequestedDate                 DATETIME NOT NULL DEFAULT Now(),
    Status                          TEXT(20) NOT NULL DEFAULT 'Draft', -- Draft/Submitted/UnderReview/Approved/Rejected/Posted
    ReviewedByEmployeeID             LONG,
    ReviewedDate                       DATETIME,
    RejectionReason                     MEMO,
    RequiredApprovalLevel                 TEXT(20),  -- from tbl_RegulatoryRules threshold
    CONSTRAINT FK_Approval_Requester FOREIGN KEY (RequestedByEmployeeID) REFERENCES tbl_Employee (EmployeeID),
    CONSTRAINT FK_Approval_Reviewer FOREIGN KEY (ReviewedByEmployeeID) REFERENCES tbl_Employee (EmployeeID)
);
CREATE INDEX IX_Approval_Entity ON tbl_Approval (EntityType, EntityID);

CREATE TABLE tbl_RegulatoryException (
    RegulatoryExceptionID  AUTOINCREMENT PRIMARY KEY,
    AccountID                LONG NOT NULL,
    RuleID                     LONG NOT NULL,      -- FK tbl_RegulatoryRules — e.g. the 6-year rule
    TriggerDate                  DATETIME NOT NULL,
    ReasonCode                     TEXT(30),
    Justification                    MEMO,
    SupportingDocumentRef              TEXT(200),
    ExpectedRecovery                     CURRENCY,
    ExpectedRecoveryDate                   DATETIME,
    ResponsibleEmployeeID                    LONG,
    ApprovalID                                 LONG,
    NextReviewDate                              DATETIME,
    Status                                        TEXT(20) DEFAULT 'Open', -- Open/Reviewed/Cleared
    CONSTRAINT FK_RegException_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);
CREATE INDEX IX_RegException_Account ON tbl_RegulatoryException (AccountID);


-- ---------------------------------------------------------------------
-- 14. IFRS 9 CONTROL LAYER
-- ---------------------------------------------------------------------

CREATE TABLE tbl_IFRS9Snapshot (
    IFRS9SnapshotID        AUTOINCREMENT PRIMARY KEY,
    AccountID                  LONG NOT NULL,
    SnapshotPeriod                DATETIME NOT NULL,
    Stage                           TEXT(10) NOT NULL,  -- 1/2/3
    PD                                DOUBLE,
    LGD                                 DOUBLE,
    EAD                                   CURRENCY,
    ECL                                     CURRENCY,
    SICRFlag                                 YESNO,
    RestructuredFlag                           YESNO,
    CuredFlag                                    YESNO,
    SourceSystemECL                                CURRENCY,  -- from the bank's official IFRS9 engine
    ReconciliationDifference                         CURRENCY, -- SourceSystemECL - ECL, must be explained if nonzero
    CONSTRAINT UQ_IFRS9_Account_Period UNIQUE (AccountID, SnapshotPeriod),
    CONSTRAINT FK_IFRS9_Account FOREIGN KEY (AccountID) REFERENCES tbl_Account (AccountID)
);

CREATE TABLE tbl_ECLBridge (
    ECLBridgeID             AUTOINCREMENT PRIMARY KEY,
    BridgePeriod                DATETIME NOT NULL,
    Scope                          TEXT(30) NOT NULL,  -- Portfolio/Product/Segment
    ScopeID                          LONG,
    OpeningECL                         CURRENCY,
    NewDefaults                          CURRENCY,
    SICRMovement                           CURRENCY,
    PortfolioChange                          CURRENCY,
    ModelParameterChange                       CURRENCY,
    ManagementOverlay                            CURRENCY,
    Recoveries                                     CURRENCY,
    WriteOffs                                        CURRENCY,
    Cures                                              CURRENCY,
    OtherMovement                                        CURRENCY,
    ClosingECL                                             CURRENCY
);

-- ---------------------------------------------------------------------
-- 15. PERIOD CLOSING
-- ---------------------------------------------------------------------

CREATE TABLE tbl_MonthClosing (
    MonthClosingID          AUTOINCREMENT PRIMARY KEY,
    ClosingPeriod                DATETIME NOT NULL,
    StepCode                       TEXT(50) NOT NULL,  -- one row per step of the 14-step sequence
    StepOrder                        INTEGER NOT NULL,
    Status                             TEXT(20) NOT NULL DEFAULT 'Pending', -- Pending/Running/Completed/Failed
    StartedAt                            DATETIME,
    CompletedAt                            DATETIME,
    ErrorMessage                             MEMO,
    CONSTRAINT UQ_MonthClosing_Period_Step UNIQUE (ClosingPeriod, StepCode)
);

CREATE TABLE tbl_YearClosing (
    YearClosingID           AUTOINCREMENT PRIMARY KEY,
    ClosingYear                  INTEGER NOT NULL,
    ClosedAt                       DATETIME,
    ClosedByEmployeeID               LONG,
    Status                             TEXT(20) NOT NULL DEFAULT 'Open',
    CONSTRAINT UQ_YearClosing_Year UNIQUE (ClosingYear)
);

CREATE TABLE tbl_PeriodLock (
    PeriodLockID            AUTOINCREMENT PRIMARY KEY,
    LockPeriod                   DATETIME NOT NULL,
    LockedAt                       DATETIME NOT NULL DEFAULT Now(),
    LockedByEmployeeID               LONG,
    CONSTRAINT UQ_PeriodLock_Period UNIQUE (LockPeriod)
);

-- ---------------------------------------------------------------------
-- 16. AUDIT TRAIL (insert-only; nothing here is ever updated/deleted)
-- ---------------------------------------------------------------------

CREATE TABLE tbl_AuditTrail (
    AuditTrailID            AUTOINCREMENT PRIMARY KEY,
    EventDateTime                DATETIME NOT NULL DEFAULT Now(),
    UserName                       TEXT(100) NOT NULL,
    EmployeeID                       LONG,
    ActionType                         TEXT(30) NOT NULL,  -- Insert/Update/Approve/Reject/Reversal/Import/Config
    TableName                            TEXT(60) NOT NULL,
    RecordID                               LONG,
    AccountID                                LONG,
    FieldName                                  TEXT(60),
    OldValue                                     MEMO,
    NewValue                                       MEMO,
    Reason                                           MEMO,
    ApprovalID                                         LONG,
    SourceContext                                        TEXT(60)  -- e.g. module/procedure name that logged it
);
CREATE INDEX IX_Audit_Table_Record ON tbl_AuditTrail (TableName, RecordID);
CREATE INDEX IX_Audit_Account ON tbl_AuditTrail (AccountID);
CREATE INDEX IX_Audit_EventDateTime ON tbl_AuditTrail (EventDateTime);
