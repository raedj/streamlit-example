# 06 — Reports, Analytics & Dashboards Catalog

Every item below is built as an Access saved query (the calculation) plus
a bound report or dashboard form (the presentation) — not hand-written
VBA — because a report's layout can only be verified by opening it in
Access, which this environment cannot do. Each entry gives: the exact
formula/grouping, its source tables, and who consumes it, so a developer
can build it directly. Where a figure should persist for trending, it is
written to `tbl_KPIResult` (`KPICode`, `Scope`, `ScopeID`, `KPIPeriod`) by
the month-end close step "12_ManagementReports" — every dashboard reads
from `tbl_KPIResult` plus light joins, never re-aggregates raw
transaction tables live (Performance §79).

## A. Portfolio reports

| Report | Source | Definition |
|---|---|---|
| NPL Portfolio | `tbl_MonthlySnapshot` | Exposure/Principal/Interest/SI/Provision by `ClassificationStatus`, latest `SnapshotPeriod` |
| Aging | `tbl_Account`, DateDiff(`DefaultDate`, today) | Buckets: 0–3/3–6/6–12/12–24/24–36/36–60/60+ months (same buckets as Recovery Vintage, §35) |
| Exposure / Provision / Suspended Interest / Collateral | `tbl_MonthlySnapshot` | Sum by product/segment/branch |

## B. Recovery reports (Daily/Monthly/YTD, by Employee/Team/Office/Product/Customer)

```sql
-- Monthly Recovery (net of the same-month off-balance rule)
SELECT Format(c.CollectionDate,'yyyy-mm') AS Period, a.AssignedEmployeeID,
       SUM(c.GrossAmount) AS GrossRecovery,
       SUM(c.GrossAmount) - SUM(Nz(rc.Amount,0)) AS NetRecovery
FROM tbl_Collection c
JOIN tbl_Account a ON a.AccountID = c.AccountID
LEFT JOIN tbl_RecoveryCost rc ON rc.CollectionID = c.CollectionID
WHERE c.IsRecovery = True AND c.ApprovalStatus = 'Posted'
GROUP BY Format(c.CollectionDate,'yyyy-mm'), a.AssignedEmployeeID
```
`IsRecovery = True` is what excludes same-month off-balance reductions
(§15/§71) — this filter must appear in every Recovery report, no
exceptions.

**Recovery Classification split** (§25): group the same query by
`RecoveryClassification` (Cash/Collateral/Legal/Settlement/Insurance/
Auction/VoluntaryTransfer/Guarantor/Other); Waiver/OffBalance/WriteOff/
ProvisionReversal are structurally absent since they never populate
`tbl_Collection`.

## C. Financial reports

| Report | Formula | Source |
|---|---|---|
| Profit / Saving | `SUM(Amount) GROUP BY ReleaseClassification` | `tbl_ProvisionSnapshot.ReleaseClassification`, `tbl_SuspendedInterest.ReversalClassification` — always shown as two columns, never merged |
| Provision Impact | Bridge columns as-is | `tbl_ProvisionSnapshot` |
| Net Recovery | `GrossAmount - SUM(RecoveryCost.Amount)` | `modRecoveryEngine.NetRecovery` |
| Cost per JOD Recovery | `SUM(RecoveryCost.Amount) / SUM(NetRecovery)` by Department/Team/Employee/ExternalOffice/LawOffice/Product/Segment | `tbl_RecoveryCost`, `tbl_Collection` |

## D. Account / Customer / Product Profitability (§28–30)

```
AccountEconomicContribution =
    NetRecovery + ProvisionRelease + SuspendedInterestRecovery
    - Waiver - LegalCosts - CollectionCosts - Commission
```
Labelled explicitly as an **internal management indicator, not an
accounting entry** in every report footer/tooltip, per §28's instruction.
Customer Profitability = the same measure `SUM`'d across all
`tbl_Account` rows sharing a `CustomerID` (or `tbl_Customer.GroupID` for
related parties). Product Profitability = grouped by `tbl_Facility.Product`.

## E. External Office & Employee performance (§31–32)

External Office: Assigned accounts, Exposure, Collection, Recovery,
Commission, Legal Costs, Net Recovery, Recovery Rate
(`NetRecovery/Exposure`), Cost per JOD, Broken Settlements, Dormant
Accounts (see §41), and **Office ROI** = `NetRecovery / (Commission +
LegalCosts)`.

Employee performance is deliberately multi-dimensional — never a single
"amount collected" ranking:
- **Recovery**: Recovery, Collection, Recovery Rate, Net Recovery.
- **Productivity**: `COUNT(tbl_Assignment)`, `COUNT(tbl_FollowUp)`,
  `COUNT(tbl_Promise)`, `COUNT(tbl_Settlement)`.
- **Quality**: Broken Promises (`tbl_Promise.Status='Broken'`), Broken
  Settlements (`tbl_Settlement.Status='Broken'`), a Data Quality
  contribution score, Follow-up Compliance (`FollowUp` present within the
  policy's required cadence — system parameter).
- **Financial**: Net Recovery, Cost per JOD Recovery.

## F. Legal & Settlement analytics (§33–34)

Legal Recovery Rate = `SUM(Collection.GrossAmount WHERE
RecoveryClassification='Legal') / SUM(LegalCase.ClaimAmount)`. Legal ROI =
`LegalRecovery / LegalCosts`. Average Time to Recovery =
`AVG(DateDiff('d', FilingDate, RecoveryDate))`.

Settlement Success Rate = `COUNT(Status='Completed') /
COUNT(*)` over `tbl_Settlement`. Settlement Recovery Rate =
`SUM(SettlementPayment.PaidAmount) / SUM(Settlement.SettlementAmount)`.

## G. Vintage, cohort, roll-rate, scoring (§35–39)

- **Recovery Vintage**: bucket accounts by
  `DateDiff('m', DefaultDate, Today)` into 0–3/3–6/6–12/12–24/24–36/
  36–60/60+ (system-parameter-defined bucket edges).
- **Cohort Analysis**: `Year(DefaultDate)` as cohort key; compare Recovery
  and Economic Contribution across cohorts.
- **Roll Rate**: transitions of `tbl_Account.AccountLifecycleStage`
  month-over-month, computed from consecutive `tbl_MonthlySnapshot` rows'
  `AccountLifecycleStage` at the corresponding date (stage history is
  reconstructed from snapshots, not stored redundantly).
- **Recovery Probability Score (0–100)**: a weighted composite —
  `Age, RecentCollection(90d), SettlementActive, CollateralCoverage,
  LegalStage, BrokenPromiseCount, BrokenSettlementCount` — weights stored
  in `tbl_SystemParameter` (`RECOVERY_SCORE_WEIGHT_*`), computed at
  month-end into `tbl_KPIResult` (`KPICode='RECOVERY_PROBABILITY'`).
  Documented everywhere as an **internal heuristic, not a regulatory
  classification** (§38).
- **Recovery Opportunity** = `ExpectedRecovery * RecoveryProbability /
  RecoveryCost`, ranked descending for the "Top Recovery Opportunities"
  list.

## H. Concentration, large exposure, dormancy (§42–44)

Top 10/20/50/100 by Customer/Group/Sector/Product/Geography/Collateral/
LegalStatus — `TOP N ... ORDER BY Exposure DESC` queries over the latest
`tbl_MonthlySnapshot`. Largest Uncovered Exposure uses
`modCollateralEngine.UncoveredExposure`. Dormant Accounts: no row in
`tbl_Collection`, `tbl_FollowUp`, `tbl_Promise`, `tbl_Settlement`, or
`tbl_LegalAction` within `DORMANCY_DAYS` (system parameter).

## I. Collateral / Auction / Voluntary Transfer analytics (§44–46)

Collateral: Original/Current/Consumed/Remaining Value, LTV
(`Exposure/CollateralValue`), Coverage, Realization Cost, Expected
Recovery — from `tbl_Collateral` + `tbl_CollateralValuation` +
`tbl_CollateralConsumption`. Auction: Reserve vs Auction Price, Discount %,
count, cost, Net Recovery, Time to Sale — from `tbl_Collection` where
`RecoveryClassification='Auction'` joined to `tbl_RecoveryCost`.
Voluntary Transfer vs Auction: side-by-side comparison of the same metrics
grouped by `RecoveryClassification IN ('VoluntaryTransfer','Auction')`.

## J. IFRS 9 control layer (§47–51)

- **ECL Movement Bridge**: `tbl_ECLBridge` columns as-is (Opening ECL +
  New Defaults + SICR + Portfolio Change + Model/Parameter Change ±
  Management Overlay − Recoveries − Write-offs − Cures + Other = Closing
  ECL), populated at month-end by summing `tbl_IFRS9Snapshot` deltas.
- **Reconciliation**: `tbl_IFRS9Snapshot.ReconciliationDifference =
  SourceSystemECL - ECL`; any nonzero value is a standing exception on the
  Regulatory Dashboard until explained — this system's IFRS 9 figures are
  always shown alongside, never instead of, the bank's official engine
  output (§47's explicit instruction).
- **Recovery vs ECL Forecast Accuracy**: `1 - ABS(ActualRecovery -
  ExpectedRecovery) / ExpectedRecovery`, tracked in `tbl_Forecast` vs
  `tbl_KPIResult`.
- **Stress Testing**: parameterized scenario queries applying
  `tbl_SystemParameter`-defined shocks (Recovery −10/−20/−30%, Collateral
  −10/−20/−30%, Cost +20/+30%) to the current snapshot and recomputing
  Recovery/Provision/ECL/UncoveredExposure/FinancialImpact — output rows
  tagged `ScenarioCode` in `tbl_KPIResult`, never overwriting the base
  case.
- **Reverse Stress Testing**: solves for the Recovery shock % at which a
  configured risk threshold (system parameter) is breached — implemented
  as an iterative search over the same stress query.

## K. Portfolio health & early warning (§52–53)

Portfolio Health Score: weighted composite of Recovery trend, Aging mix,
Provision Coverage, Collateral Coverage, Uncovered Exposure, Broken
Settlements rate, Dormant Accounts rate, Legal Recovery rate — weights in
`tbl_SystemParameter`; trend of the last 3 periods classifies
Improving/Stable/Deteriorating. Early Warning alerts: one row per
breached threshold (Recovery down, Waiver up, Broken Settlements up,
Uncovered Exposure up, >3yr accounts up, >6yr exceptions up, cost/JOD up,
Legal Recovery down, Collateral Coverage down) — each threshold itself a
`tbl_RegulatoryRules`/`tbl_SystemParameter` row, surfaced on
`frmExecutiveDashboard`.

## L. Executive / Board / Regulatory dashboards (§54–56)

**Board Dashboard** (`frmBoardDashboard`) — five panels exactly as
specified: Portfolio (Exposure/Accounts/Aging/Provision/SI/Uncovered),
Recovery (Monthly/YTD/Target/Forecast/Net), Risk (Top Exposures,
Concentration, >3yr, >6yr, Exceptions), Financial (Profit/Saving/
Provision Impact/Recovery Contribution/Cost), Strategy (Settlement/Legal/
Auction/Off-Balance/Write-off volumes).

**Executive Management Dashboard** (`frmExecutiveDashboard`) — single
scorecard: Portfolio, Recovery, Target, Forecast, Provision, SI, Net
Recovery, Profit, Saving, Uncovered Exposure, 3-Year Accounts, 6-Year
Exceptions, Legal Recovery, Settlement Success, External Office ROI, Cost
per JOD Recovery — each a `tbl_KPIResult` row for the latest period plus a
12-month sparkline.

**Regulatory Dashboard** (`frmRegulatoryDashboard`) — exception-first
view, config-driven throughout (queries filter on `tbl_RegulatoryRules`
thresholds, never a literal): Classification Exceptions, Provision
Exceptions, Missing Data (from `tbl_ImportError`/`tbl_DataQuality`), >3yr
and >6yr accounts (from `modOffBalanceRulesEngine` output +
`tbl_RegulatoryException`, with `ExceptionAgingBucket`), Missing
Justification/Approval, Unexplained Movement (`tbl_MovementException`),
Manual Collection Exceptions (`tbl_Collection.Source='Manual'` pending
approval), Settlement/Waiver/Collateral Exceptions.

## Data lineage note

Every query above filters or joins on `BatchID` / `SnapshotID` so a number
on any dashboard traces back to source per `03_ERD.md`'s lineage chain —
build each saved query to select these lineage keys even when the bound
report doesn't display them, so `frmAccountMaster`'s "show source" link
can always resolve.
