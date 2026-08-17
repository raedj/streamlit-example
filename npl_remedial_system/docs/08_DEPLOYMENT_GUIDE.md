# 08 — Deployment Guide

## 1. Build the back-end

1. Create a blank `NPL_Backend.accdb`.
2. Open a new Query in SQL View, paste each `CREATE TABLE` /
   `CREATE INDEX` statement from `sql/schema.sql` **one at a time** (Jet
   SQL does not run multi-statement scripts) and run it, in the order the
   file lists them (parent tables before the foreign keys that reference
   them — the file is already ordered this way).
   - Faster alternative: a short VBA bootstrap loop that reads
     `schema.sql`, splits on `;`, and calls `CurrentDb.Execute` per
     statement — write this once in `modSchemaBootstrap` (not included
     here since it's a one-time dev utility, not a runtime engine).
3. Verify every table, relationship, and index against `03_ERD.md`.
4. Seed `tbl_SystemParameter` and `tbl_RegulatoryRules` — see
   `10_CONFIGURATION_PARAMETERS.md` for the minimum required rows (the
   system will not run without these; every engine raises rather than
   silently defaulting).

## 2. Build the front-end

1. Create `NPL_Frontend.accdb`.
2. *External Data → Link Tables* to `NPL_Backend.accdb` for every table.
3. *File → Import → Module* each `.bas` file from `vba/`, in this order
   (respects dependency): `modUtilities`, `modAuditTrail`,
   `modRegulatoryRulesEngine`, `modImportEngine`,
   `modDataQualityEngine`, `modMovementEngine`, `modRecoveryEngine`,
   `modCollateralEngine`, `modOffBalanceRulesEngine`,
   `modProvisionSuspendedInterestEngine`, `modApprovalWorkflowEngine`,
   `modMonthEndClosingEngine`.
4. Add references (VBA Editor → Tools → References): Microsoft DAO 3.6+,
   Microsoft Excel Object Library (for `modImportEngine`'s late-bound
   Excel automation — kept late-bound with `CreateObject` in code, so the
   reference is optional but recommended for IntelliSense during
   development).
5. Build the forms per `05_FORMS_BLUEPRINT.md`.
6. `Debug → Compile` before first run — with `Option Explicit` on every
   module, this catches typos/undeclared variables immediately.

## 3. File system

Create the folder tree from `01_ARCHITECTURE.md` on a shared network
location both the back-end and all front-end users can reach:
```
\NPL_System\{Incoming,Processed,Rejected,Archive,Reports,Logs,Backup}\
```
Update `modImportEngine`'s `INCOMING_FOLDER` / `PROCESSED_FOLDER` /
`REJECTED_FOLDER` constants and `modUtilities.LOG_FOLDER` to the actual
UNC path before go-live.

## 4. Scheduling

- **Import cycle**: Windows Task Scheduler, running
  `MSACCESS.EXE "NPL_Frontend.accdb" /x mcrRunImportCycle` (an AutoExec
  macro calling `modImportEngine.RunImportCycle`) on the daily/weekly
  cadence the bank requires.
- **Month-end close**: triggered manually from `frmMonthEndClose` by an
  Admin/Department Manager after the final monthly file is confirmed
  imported — deliberately not fully automated, since closing is a
  controlled event requiring human confirmation that the period's data is
  complete.

## 5. Backups (§78)

- **Daily**: scheduled task copies `NPL_Backend.accdb` to
  `\NPL_System\Backup\Daily\` (compact-and-repair first via
  `Application.CompactRepair`), retained per `BACKUP_RETENTION_DAILY`
  system parameter.
- **Weekly / Monthly**: same mechanism, longer retention, separate
  subfolders — never overwrite the prior period's backup file.
- **Restore procedure**: stop all front-end sessions, rename the current
  back-end aside (don't delete), copy the chosen backup into place, re-
  test with one front-end before reopening broadly.

## 6. Migration path to SQL Server (per §73/§80 — build for this, don't do it yet)

Because tables are linked (not embedded) and no business rule lives in a
VBA constant:

1. Use Access's *Database Tools → SQL Server Migration Wizard* (or
   `ssma` for Access) against `NPL_Backend.accdb`.
2. Type mapping used consistently in `schema.sql`: `AUTOINCREMENT →
   IDENTITY(1,1)`, `CURRENCY → DECIMAL(19,4)`, `YESNO → BIT`,
   `TEXT(n) → NVARCHAR(n)`, `MEMO → NVARCHAR(MAX)`, `DATETIME → DATETIME2`.
3. Re-link the front-end's tables via ODBC to the new SQL Server database
   — no VBA changes required, since every engine module uses
   `CurrentDb.OpenRecordset`/`.Execute` against linked tables rather than
   local-only Jet features (no Jet-specific SQL extensions were used in
   the engine modules beyond standard DAO).
4. Re-point `modUtilities.LOG_FOLDER` and the import folders if the
   deployment topology changes at the same time.

## 7. Go-live checklist

- [ ] All tables/indexes created and cross-checked against `03_ERD.md`
- [ ] `tbl_SystemParameter` and `tbl_RegulatoryRules` seeded (see
      `10_CONFIGURATION_PARAMETERS.md`)
- [ ] `tbl_Employee.WindowsLogin` populated for every user, roles assigned
- [ ] Folder tree created with correct share permissions
- [ ] One full import cycle run and manually verified end-to-end
- [ ] One full month-end close run on a test period, all 14 steps green
- [ ] Backup job scheduled and one restore drill performed
- [ ] `09_TESTING_PLAN.md` executed
