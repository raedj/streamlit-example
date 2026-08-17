# Remedial & NPL Portfolio Management System
### Microsoft Access + VBA — Design & Source Package

## What this is

A production-grade design and source package for a Microsoft Access + VBA
system that manages a bank's Non-Performing Loans (NPL) / Remedial
portfolio: periodic Excel import, monthly snapshots, collections,
settlements, legal cases, collateral, provisions, suspended interest,
off-balance rules, an IFRS 9 control layer, analytics, and executive /
board / regulatory dashboards — built so the sensitive rules (collateral
consumption period, 3-year / 6-year off-balance thresholds, etc.) are
**configuration-driven**, not hard-coded.

## Important: how to read this package

This package was authored in a headless Linux environment with no Windows
or Microsoft Access runtime available, so nothing here has been compiled,
opened in Access, or executed. It is delivered as **source truth**, not a
binary `.accdb`:

- `sql/schema.sql` — complete DDL for every table (Jet/ACE SQL dialect,
  Access-compatible; portable to SQL Server per the migration notes in
  `docs/08_DEPLOYMENT_GUIDE.md`).
- `vba/*.bas` — full VBA module source for every computational engine
  (movement, recovery, collateral consumption, off-balance 3yr/6yr rules,
  provision & suspended-interest bridges, maker-checker approval workflow,
  audit trail, month-end closing, config-driven regulatory rules).
- `docs/*.md` — architecture, ERD, forms blueprint, report/dashboard
  catalog with KPI formulas, security & roles, deployment guide, testing
  plan, and the configuration-parameter catalog.

**To build the real system**: create a blank `.accdb` back-end, run
`sql/schema.sql` (via Access's query designer / VBA `CurrentDb.Execute`,
statement by statement — Jet SQL does not support multi-statement scripts),
import each `.bas` file via *File → Import → Module*, then build the forms
and reports described in `docs/05_FORMS_BLUEPRINT.md` and
`docs/06_REPORTS_AND_DASHBOARDS.md` against these tables/engines. Everything
needs testing inside real Access before go-live — treat this as a
complete, reviewed blueprint, not a tested build.

## Scope note

Your original spec (80 sections) is fully covered, but not every section is
literal VBA line-by-line:

- **Fully coded as VBA engines**: import/staging, data quality, movement
  reconciliation, recovery waterfall & classification, collateral
  consumption, off-balance 3-year/6-year rules + exception aging, provision
  & suspended-interest movement bridges (incl. Profit/Saving split),
  maker-checker approval workflow, audit trail (reversal-not-delete),
  month-end/year-end closing orchestration, and the configurable
  regulatory-rules engine.
- **Fully specified as schema + config tables**: every entity in section 72
  (customers, accounts, snapshots, collections, settlements, legal cases,
  collateral, employees, teams, external offices, approvals, import
  batches, audit trail, closings, targets/forecasts, KPI results...).
- **Fully specified as documentation (queries/report defs, not raw VBA)**:
  the ~35 analytics/report/dashboard sections (vintage, cohort, roll-rate,
  recovery probability, concentration, stress testing, executive/board/
  regulatory dashboards, employee/office/legal/settlement analytics). Access
  reports and dashboard queries are normally built in the Access report
  designer against saved queries, not hand-written as VBA — so these are
  given as precise query/report specifications (fields, grouping, formulas,
  source tables) that a developer builds directly in Access. Building them
  as literal `.bas` text without a way to render/verify a report layout
  would produce unverifiable code, not a usable report.

## Folder map

```
npl_remedial_system/
├── README.md
├── sql/
│   └── schema.sql                    Full DDL — all tables, keys, indexes
├── vba/
│   ├── modUtilities.bas              Config/parameter reader, logging, error helper
│   ├── modAuditTrail.bas             Audit logging, reversal-not-delete
│   ├── modImportEngine.bas           Excel folder scan, template validation, batching
│   ├── modDataQualityEngine.bas      Pre-commit validation + DQ score
│   ├── modMovementEngine.bas         Opening→Closing reconciliation, unexplained-movement
│   ├── modRecoveryEngine.bas         Financial waterfall, recovery classification
│   ├── modCollateralEngine.bas       60-month consumption schedule from Default Date
│   ├── modOffBalanceRulesEngine.bas  3-year review + 6-year mandatory escalation
│   ├── modProvisionSuspendedInterestEngine.bas  Provision/SI bridges, Profit vs Saving
│   ├── modApprovalWorkflowEngine.bas Maker-checker state machine
│   ├── modMonthEndClosingEngine.bas  Closing orchestration + period lock
│   └── modRegulatoryRulesEngine.bas  Reads tbl_RegulatoryRules / tbl_SystemParameters
└── docs/
    ├── 01_ARCHITECTURE.md
    ├── 03_ERD.md         (schema DDL itself lives in sql/schema.sql)
    ├── 04_ENGINES_SPEC.md
    ├── 05_FORMS_BLUEPRINT.md
    ├── 06_REPORTS_AND_DASHBOARDS.md
    ├── 07_SECURITY_AND_ROLES.md
    ├── 08_DEPLOYMENT_GUIDE.md
    ├── 09_TESTING_PLAN.md
    └── 10_CONFIGURATION_PARAMETERS.md
```

## Non-negotiable rules encoded throughout

- `DefaultDate` — never `SystemEntryDate` — is the anchor for aging,
  collateral consumption, and the 3-year/6-year off-balance clock.
- Off-Balance ≠ Recovery ≠ Write-off ≠ Waiver ≠ Provision Reversal. These are
  tracked as distinct movement types everywhere (schema, engines, reports).
- A same-month off-balance movement is never counted as that month's
  recovery.
- No manual, financially-impactful entry posts without maker-checker
  approval; no approved transaction is ever hard-deleted — only reversed.
- Regulatory thresholds (collateral months, 3-year/6-year limits, etc.) live
  in `tbl_SystemParameters` / `tbl_RegulatoryRules`, never as VBA constants.
