# 05 — Forms Blueprint

Access forms cannot be represented as portable text (they are stored as
compiled objects inside the `.accdb` binary), so this is a build
specification for a developer working directly in the Access Form
Designer, bound to the tables in `sql/schema.sql` and wired to the `vba/`
engines.

## Navigation shell

`frmMain` (Navigation Form) — role-based ribbon (per `07_SECURITY_AND_ROLES.md`):
Portfolio | Accounts | Import | Approvals | Legal | Collateral | Analytics |
Dashboards | Admin.

## Account-centric forms

### `frmAccountMaster`
Bound to `tbl_Account` (main form) with a tab control of subforms:
- **Overview**: customer/facility header, all seven lifecycle dates
  (Default/NPL/SystemEntry/OffBalance/WriteOff/Settlement/Closure) shown
  side by side — deliberately never collapsed into one "date" field, per
  §6 of the spec.
- **Timeline** (`subAccountTimeline`, read-only): renders the §69 sequence
  (Default → NPL → First Contact → Promise → Collection → Settlement →
  Broken Settlement → Legal → Auction/Transfer → Write-off/Off-Balance →
  Post-write-off Recovery → Closure) by unioning `tbl_FollowUp`,
  `tbl_Promise`, `tbl_Collection`, `tbl_Settlement`, `tbl_LegalAction`,
  `tbl_OffBalance`, `tbl_WriteOff`, ordered by event date.
- **Collections** (`subCollections`): `tbl_Collection` +
  `tbl_CollectionAllocation` waterfall breakdown per row.
- **Collateral** (`subCollateral`): `tbl_Collateral` +
  `tbl_CollateralConsumption` current-period coverage.
- **Legal** (`subLegalCases`): `tbl_LegalCase` + `tbl_LegalAction`.
- **Settlement** (`subSettlement`): `tbl_Settlement` +
  `tbl_SettlementPayment` schedule with Paid/Missed status.
- **Notes & Follow-up** (`subNotes`): `tbl_AccountNote`, `tbl_FollowUp`,
  `tbl_Promise`.
- **Strategy** (`subStrategy`, read-only): the §40/§70 recommended-action
  panel — sources `tbl_KPIResult` rows written by the analytics queries in
  `06_REPORTS_AND_DASHBOARDS.md` (Recovery Probability, Recovery
  Opportunity). Always rendered as a suggestion with a "not auto-executed"
  banner.

### `frmManualCollectionEntry` / `frmWaiverEntry` / `frmSettlementEntry` /
### `frmWriteOffEntry` / `frmOffBalanceEntry`
Each: Draft-only entry form. On Save, calls
`modApprovalWorkflowEngine.SubmitForApproval`. Read-only once
`ApprovalStatus <> 'Draft'`. The requester cannot see an "Approve" button
for their own request — enforced both by hiding the control when
`CurrentEmployeeID() = RequestedByEmployeeID` and, as defense in depth, by
`modApprovalWorkflowEngine.ReviewApproval`'s hard block.

## Approval

### `frmApprovalQueue`
List view of `tbl_Approval` filtered to `Status IN ('Submitted',
'UnderReview')` and `RequiredApprovalLevel` matching the current
manager's role. Approve/Reject buttons call `ReviewApproval`; Reject
requires a non-blank reason.

## Import

### `frmImportMonitor`
Shows `tbl_ImportBatch` history, a "Run Import Now" button
(`modImportEngine.RunImportCycle`), live `tbl_ImportFile`/`tbl_ImportError`
drill-down, and the current `tbl_DataQuality` score per batch.

### `frmExcelTemplateAdmin`
Admin-only CRUD on `tbl_ExcelTemplate` (Admin role, `CanConfigure`).

## Legal

### `frmLegalCaseMaster` / `subLegalActions` / `subLegalNotes`

## Collateral

### `frmCollateralMaster` with `subValuations` and a read-only
`subConsumptionSchedule` (chart: consumed vs remaining over
`CollateralConsumptionMonths`).

## Admin / Configuration

### `frmSystemParameters` — CRUD on `tbl_SystemParameter`.
### `frmRegulatoryRules` — CRUD on `tbl_RegulatoryRules`, calling
`modRegulatoryRulesEngine.UpdateRuleThreshold` (never a direct table edit,
so history is preserved and audited).
### `frmUserAdmin` — `tbl_Employee` / `tbl_UserRole` management.
### `frmMonthEndClose` — triggers `modMonthEndClosingEngine.RunMonthEndClose`,
shows the 14-step `tbl_MonthClosing` progress live, and disables itself for
a period once `modMonthEndClosingEngine.IsPeriodLocked` returns True.

## Dashboards

See `06_REPORTS_AND_DASHBOARDS.md` — each dashboard is one form
(`frmExecutiveDashboard`, `frmBoardDashboard`, `frmRegulatoryDashboard`,
`frmLegalAnalytics`, `frmEmployeePerformance`, etc.) bound to a saved query
or `tbl_KPIResult`, using native Access charting controls
(`Microsoft Chart Control` or Modern Charts in Access 2019+).
