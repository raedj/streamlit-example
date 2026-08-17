# 09 — Testing Plan

None of this has been executed (no Access runtime in this build
environment) — this is the test plan to run before go-live, organized by
engine, with concrete pass/fail criteria tied to the rules in
`04_ENGINES_SPEC.md`.

## Unit-level (per engine, using a small seeded test back-end)

| Engine | Test | Pass criteria |
|---|---|---|
| Import | Drop the same file twice | Second import rejected with `FileHash` duplicate reason; no new `tbl_MonthlySnapshot` rows |
| Import | Drop a file with a renamed column | Rejected as Template Version Error, not silently loaded |
| Data Quality | Row with `Provision > Exposure` | Row `ValidationStatus='Invalid'`, `tbl_ImportError` row created, excluded from DQ score numerator |
| Movement | Manually break the bridge (mismatched closing) | `tbl_MovementException` created; balance NOT silently forced to match; `IsRecovery` never set on the gap |
| Recovery | Post an Off-Balance transaction and a Collection in the same month for the same account | Off-Balance row has `IsRecovery=False`; only the Collection row counts toward Monthly Recovery report |
| Recovery | Waterfall on a collection larger than Principal+Interest+SI+Provision | Remainder correctly rolls through every step in order; no step allocated more than its capacity |
| Collateral | Account with `DefaultDate` 65 months ago, 60-month consumption parameter | `RemainingValue = 0`, `ConsumedToDate = OriginalValue` |
| Collateral | Change `COLLATERAL_CONSUMPTION_MONTHS` mid-year | New accounts' consumption schedule uses the new value; already-computed historical `tbl_CollateralConsumption` rows for prior periods are unchanged (append-only) |
| Off-Balance | Account crosses 6-year threshold with no exception on file | `tbl_RegulatoryException` auto-created exactly once (re-running month-end doesn't duplicate it) |
| Off-Balance | Exception `NextReviewDate` 400 days in the past | `ExceptionAgingBucket` returns `>365` |
| Provision/SI | Reversal of a balance carried in from the prior year | `ReleaseClassification = 'Profit'` |
| Provision/SI | Reversal of a balance both created and released this year | `ReleaseClassification = 'Saving'` |
| Approval | Manager tries to approve their own submission | `ReviewApproval` raises error 5401, no state change |
| Approval | Approved Collection | Posts, `tbl_Movement` row appears, waterfall allocated |
| Audit/Reversal | Attempt to `UPDATE` a Posted `tbl_Collection` row directly | `AssertNotPosted` raises error 5101 |
| Audit/Reversal | `CreateReversal` on a posted movement | Equal-and-opposite row created referencing original via `SourceReferenceID`, itself requires approval |
| Month-End | Run close twice on the same period without fixing anything | Second run skips already-`Completed` steps, does not duplicate `tbl_MonthlySnapshot` rows (blocked by `UQ_Snapshot_Account_Period`) |
| Month-End | Attempt any write to a locked period outside `CreateReversal` | Blocked (period-lock check in the relevant form/engine entry point) |
| Regulatory Rules | Query `GetActiveRuleThreshold` for a rule with no active row | Raises error 5601, does not return a default |
| Regulatory Rules | `UpdateRuleThreshold` | Old row gets `ExpiryDate`, new row inserted, `tbl_AuditTrail` shows both old and new values |

## Integration-level

1. **End-to-end monthly cycle**: seed ~50 test accounts spanning ages
   0–7 years, run Import → Data Quality → full Month-End Close, verify:
   - Every account has exactly one new `tbl_MonthlySnapshot` row.
   - Accounts ≥3 years show an `OFFBALANCE_3YR_ELIGIBLE` KPI row.
   - Accounts ≥6 years and still on-balance each have exactly one open
     `tbl_RegulatoryException`.
   - Movement bridge reconciles for every account with zero
     `tbl_MovementException` rows (seed data should tie out exactly).
2. **Reconciliation drill**: deliberately seed one account with a source
   file closing balance that doesn't match the computed bridge; confirm
   it surfaces on the Regulatory Dashboard's Unexplained Movement panel
   and nowhere silently "fixes itself."
3. **Historical reload**: import a full year of monthly files for the
   same account set as a `HistoricalReload` batch type; confirm
   `tbl_MonthlySnapshot` at any past month-end can be queried and matches
   what was originally reported that month (append-only guarantee).
4. **10k-account volume/performance**: seed or synthetically generate
   10,000 accounts' worth of monthly snapshot rows; time the full
   month-end close; confirm no step relies on per-row `DLookup`/`DSum`
   calls fired from a form (Performance §79) — the engine modules here
   use batch DAO recordsets and set-based `Execute` calls throughout, but
   this should be re-verified against the actual seeded volume once built.

## UAT (user acceptance)

- One full cycle walked through by an actual Employee/Team Manager/Dept
  Manager/Admin/Audit user in their real role, confirming the row-level
  and action-level security in `07_SECURITY_AND_ROLES.md` behaves as
  designed (an Employee cannot see another officer's accounts; a Team
  Manager cannot approve their own submission; Audit can read everything
  and write nothing).
- Executive/Board/Regulatory dashboards reviewed by their actual
  intended audience for correctness of the figures against a manually
  computed spot-check on 3–5 accounts.
