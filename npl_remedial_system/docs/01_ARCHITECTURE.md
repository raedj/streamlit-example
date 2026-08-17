# 01 — Architecture

## Layered data flow

```
Source Excel Files (Incoming\)
        │  modImportEngine: scan, template-version check, batch create
        ▼
Staging Tables (stg_*)
        │  modDataQualityEngine: validation rules, DQ score
        ▼
Data Quality Gate  ───(fail)──▶ Rejected\ + tbl_ImportErrors
        │ (pass)
        ▼
Historical Snapshot (tbl_MonthlySnapshot) ── never overwritten, append-only
        │
        ▼
Movement Engine (tbl_Movement) ── Opening + New − Collections − Waivers
        │                          − WriteOffs − OffBalance − OtherReductions
        │                          = Closing; reconciled to source file
        ▼
Recovery Engine (tbl_Collection, tbl_CollectionAllocation)
        │  waterfall: Gross → Principal → Interest → Suspended Interest →
        │  Provision Impact → Waiver → Collateral Impact → Costs → Net Recovery
        ▼
Financial Impact Engine (tbl_ProvisionSnapshot, tbl_SuspendedInterest,
        │  tbl_CollateralConsumption)
        ▼
Risk / IFRS 9 Control Layer (tbl_IFRS9Snapshot, ECL bridge, reconciliation
        │  to Finance/Risk's official engine — this system never replaces it)
        ▼
Analytics Engine (vintage, cohort, roll-rate, recovery probability, scoring)
        ▼
Management Dashboards (Executive / Board / Regulatory / Legal / Employee)
        ▼
Regulatory Reporting (CBJ-facing extracts, config-driven rule thresholds)
        ▼
Audit Trail (tbl_AuditTrail) ── every write, everywhere, all layers
```

## Engine responsibilities (maps to `vba/*.bas`)

| Layer | Module | Responsibility |
|---|---|---|
| Import | `modImportEngine` | Detects files in `Incoming\`, validates against `tbl_ExcelTemplate`, creates `tbl_ImportBatch` / `tbl_ImportFile`, loads to staging, moves file to `Processed\` or `Rejected\` |
| Data Quality | `modDataQualityEngine` | Row-level checks (duplicate account, missing CIF, invalid dates, negative values, collection > outstanding, provision > exposure, missing DefaultDate/classification/collateral) → `tbl_DataQuality` score per batch |
| Movement | `modMovementEngine` | Opening/closing balance reconciliation per account per month; raises `Unexplained Movement Exception` when the bridge doesn't tie to the source closing balance |
| Recovery | `modRecoveryEngine` | Applies the financial waterfall to each collection; classifies recovery type (Cash/Collateral/Legal/Settlement/Insurance/Auction/Voluntary Transfer/Guarantor/Other); enforces the same-month off-balance ≠ recovery rule |
| Collateral | `modCollateralEngine` | Straight-line consumption from `DefaultDate` over `CollateralConsumptionMonths` (parameter, default 60); computes remaining collateral, coverage, uncovered exposure |
| Off-Balance Rules | `modOffBalanceRulesEngine` | 3-year eligibility screen (provision coverage, SI coverage, legal/collateral review, recovery potential); 6-year mandatory escalation → `tbl_RegulatoryException` with reason/justification/approval/next-review |
| Provision & SI | `modProvisionSuspendedInterestEngine` | Opening→Closing bridges for both; classifies reversals as Profit (prior Dec-31 balances) vs Saving (current-year formation) |
| Approval Workflow | `modApprovalWorkflowEngine` | Draft → Submit → Manager Review → Approved/Rejected → Posted state machine; blocks self-approval |
| Audit Trail | `modAuditTrail` | Old/new value logging on every mutating operation; enforces reversal-not-delete on posted/approved records |
| Month-End Closing | `modMonthEndClosingEngine` | Orchestrates the 14-step closing sequence (§60 of the spec) and locks the period |
| Regulatory Rules | `modRegulatoryRulesEngine` | Single point of truth for every configurable threshold — no engine reads a hard-coded number |

## Front-end / back-end split

- **Back-end** (`NPL_Backend.accdb`): all tables only. Lives on a shared
  network location or SQL Server later (see `08_DEPLOYMENT_GUIDE.md`).
- **Front-end** (`NPL_Frontend.accdb`, one copy per user): forms, reports,
  VBA modules, linked tables to the back-end. Front-end holds no data —
  it can be redeployed/updated without touching production data.
- This split, plus keeping all business rules in linked tables rather than
  VBA constants, is what makes a later SQL Server migration a re-link
  exercise (`TransferDatabase` / ODBC linked tables) instead of a rewrite.

## File-system contract (`modImportEngine`)

```
\NPL_System\
    Incoming\      New Excel files land here (daily/weekly/monthly)
    Processed\      Successfully imported files, renamed with batch stamp
    Rejected\        Failed files, with a companion .log explaining why
    Archive\          Processed files older than the retention parameter
    Reports\          Generated report/export output
    Logs\             Import, engine, and error logs
    Backup\           Daily/weekly/monthly back-end backups
```

## Design principles carried through every engine

1. **Configuration over hard-coding.** Every threshold (collateral months,
   3-year/6-year limits, aging buckets, stress-test shocks) is a row in
   `tbl_SystemParameter` or `tbl_RegulatoryRules`, versioned by
   `EffectiveDate`/`ExpiryDate`.
2. **Append, don't overwrite.** Snapshots, movements, and audit entries are
   insert-only. Corrections are reversing entries, never `UPDATE`/`DELETE`
   on posted rows.
3. **Batch-oriented, not row-by-row UI-triggered.** Engines run over a
   `BatchID` or `SnapshotPeriod` using set-based `INSERT INTO ... SELECT` /
   DAO recordset batch loops — not `DLookup` calls fired per-row from a form,
   which does not scale to 10k+ accounts × years of history.
4. **Every number is traceable.** Dashboard figure → report query → snapshot
   table → import batch → source Excel row (see `03_ERD.md` for the
   lineage keys: `SnapshotID`, `BatchID`, `SourceRowRef`).
