# 10 — Configuration Parameter Catalog

Minimum seed data for `tbl_SystemParameter` and `tbl_RegulatoryRules`.
Every engine module raises an error rather than defaulting when one of
these is missing (see `04_ENGINES_SPEC.md`), so all of this must exist
before the first import cycle or month-end close runs. Values below are
illustrative defaults matching the spec's stated numbers (60-month
collateral consumption, 3-year/6-year off-balance) — the bank's actual
policy/CBJ-approved thresholds should be confirmed before go-live and
entered exactly as approved, not assumed from this document.

## `tbl_SystemParameter`

| ParameterCode | DataType | Example value | Used by |
|---|---|---|---|
| `COLLATERAL_CONSUMPTION_MONTHS` | Integer | `60` | `modCollateralEngine` |
| `MOVEMENT_TOLERANCE` | Currency | `0.01` | `modMovementEngine` |
| `DORMANCY_DAYS` | Integer | `90` | Dormant Accounts report (§41) |
| `BACKUP_RETENTION_DAILY` | Integer | `14` | Backup job (§78) |
| `BACKUP_RETENTION_WEEKLY` | Integer | `12` | Backup job |
| `BACKUP_RETENTION_MONTHLY` | Integer | `84` | Backup job (7 years) |
| `RECOVERY_SCORE_WEIGHT_AGE` | Double | `0.15` | Recovery Probability Score (§38) |
| `RECOVERY_SCORE_WEIGHT_RECENT_COLLECTION` | Double | `0.25` | Recovery Probability Score |
| `RECOVERY_SCORE_WEIGHT_SETTLEMENT` | Double | `0.15` | Recovery Probability Score |
| `RECOVERY_SCORE_WEIGHT_COLLATERAL` | Double | `0.20` | Recovery Probability Score |
| `RECOVERY_SCORE_WEIGHT_LEGAL` | Double | `0.10` | Recovery Probability Score |
| `RECOVERY_SCORE_WEIGHT_BROKEN_PROMISE` | Double | `-0.10` | Recovery Probability Score |
| `RECOVERY_SCORE_WEIGHT_BROKEN_SETTLEMENT` | Double | `-0.15` | Recovery Probability Score |
| `STRESS_RECOVERY_SHOCK_1/2/3` | Double | `-0.10 / -0.20 / -0.30` | Stress Testing (§50) |
| `STRESS_COLLATERAL_SHOCK_1/2/3` | Double | `-0.10 / -0.20 / -0.30` | Stress Testing |
| `STRESS_COST_SHOCK_1/2` | Double | `0.20 / 0.30` | Stress Testing |
| `FOLLOWUP_REQUIRED_CADENCE_DAYS` | Integer | `30` | Follow-up Compliance KPI (§32) |

Each row also needs `EffectiveDate` (and optionally `ExpiryDate`) —
`GetSystemParameter` resolves the value in force as of the calculation
date, so historical calculations remain correct even after a parameter is
later changed.

## `tbl_RegulatoryRules`

| RuleCode | RuleType | ThresholdValue | ThresholdUnit | Notes |
|---|---|---|---|---|
| `OFFBALANCE_3YEAR` | Threshold | `3` | Years | 3-year eligibility screen anchor (§9) |
| `OFFBALANCE_6YEAR` | Threshold | `6` | Years | 6-year mandatory escalation anchor (§10) |
| `PROVISION_COVERAGE_MIN` | Threshold | *bank policy* | Percent | Input to 3-year eligibility screen |
| `SI_COVERAGE_MIN` | Threshold | *bank policy* | Percent | Input to 3-year eligibility screen |
| `APPROVAL_LEVEL_COLLECTION` | Threshold | *bank policy* | Amount | Amount above which Dept Manager (not Team Manager) must approve a manual collection |
| `APPROVAL_LEVEL_WAIVER` | Threshold | *bank policy* | Amount | Same, for waivers |
| `APPROVAL_LEVEL_SETTLEMENT` | Threshold | *bank policy* | Amount | Same, for settlements |
| `APPROVAL_LEVEL_WRITEOFF` | Threshold | *bank policy* | Amount | Same, for write-offs |
| `APPROVAL_LEVEL_OFFBALANCE` | Threshold | *bank policy* | Amount | Same, for off-balance transactions |
| `APPROVAL_LEVEL_ADJUSTMENT` | Threshold | *bank policy* | Amount | Same, for reversal/adjustment entries |

Every `RegulatoryRules` row also carries `RegulatorySource` (e.g. the
specific CBJ circular / internal policy reference) and
`CalculationMethod`/`RequiredAction` free text — filled in from the bank's
actual approved policy documents, not invented here. Where the bank's
current classification scheme (Watch/Substandard/Doubtful/Loss or
equivalent) differs from a simple threshold, model it as a
`RuleType='Classification'` row referencing the bank's documented
methodology rather than trying to force it into a single numeric
threshold.

## `tbl_ExcelTemplate`

At least one row per recurring source file (e.g. "Daily NPL Extract",
"Monthly Snapshot", "Weekly Collections") with `RequiredColumnsJSON`
describing the exact column order/names/types the bank's core-banking
export produces — this is the contract `modImportEngine.ResolveTemplate`
checks against, and should be updated (as a new `TemplateVersion` row,
not an edit to the old one) whenever the source system's export format
changes.
