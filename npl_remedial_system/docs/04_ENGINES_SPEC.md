# 04 — Engine Business Rules

Precise rules implemented by each `vba/*.bas` module. These are the rules
your spec called out as non-negotiable; each is enforced in code, not left
to convention.

## Movement categories (used everywhere: `tbl_Movement.MovementCategory`)

| Category | Meaning | Examples |
|---|---|---|
| **Portfolio** | Changes the on-balance exposure | New movement, Off-Balance, Write-off |
| **Recovery** | Real cash/asset recovered | Cash collection, auction proceeds, settlement payment |
| **Financial** | P&L / provision / suspended-interest impact | Waiver, provision release, SI reversal |

A single event can touch more than one category (e.g. a cash collection is
both Portfolio-reducing and Recovery), but **Off-Balance, Write-off, and
Waiver are never also flagged `IsRecovery = True`** — this is the rule from
§14/§71 of the spec and it's enforced in `modRecoveryEngine.ClassifyMovement`.

## Movement Engine (`modMovementEngine`)

```
ClosingBalance = OpeningBalance
                + NewMovements
                - Collections
                - Waivers
                - WriteOffs
                - OffBalanceReductions
                - OtherApprovedReductions
```

- Computed per `AccountID` per `MovementPeriod` from `tbl_Movement` rows
  tagged to that account/period.
- Compared against the source file's reported closing balance
  (`stg_AccountSnapshot.ExposureText` for that period, converted).
- Any difference beyond `MOVEMENT_TOLERANCE` (system parameter, default
  0.01) creates a `tbl_MovementException` row with
  `ExceptionType = 'Unexplained Movement'` and `IsRecovery` is **never**
  inferred from the gap — an unreconciled difference is not treated as a
  collection.

## Same-month Off-Balance rule (`modRecoveryEngine.ApplySameMonthOffBalanceRule`)

If `tbl_OffBalance.OffBalanceDate` falls in the same `MovementPeriod` as a
reduction in exposure, the corresponding `tbl_Movement` row is written with:

```
MovementType   = 'OffBalance'
MovementCategory = 'Portfolio'
IsRecovery     = False
```

regardless of how large the balance reduction is. This is checked before
any recovery total is computed, so monthly/YTD Recovery reports can never
silently include an off-balance reduction.

## Recovery Engine — financial waterfall (`modRecoveryEngine.AllocateWaterfall`)

For each `tbl_Collection` row, `GrossAmount` is allocated down the fixed
waterfall order into `tbl_CollectionAllocation`:

```
1. Principal
2. Interest
3. Suspended Interest
4. Provision Impact
5. Waiver
6. Collateral Impact
7. Costs
   ------------------
   Net Recovery = Gross Amount - Costs (from tbl_RecoveryCost)
```

Allocation stops at each step's outstanding balance for the account before
moving to the next step; unallocated remainder rolls to the next step.
`RecoveryClassification` (Cash/Collateral/Legal/Settlement/Insurance/
Auction/VoluntaryTransfer/Guarantor/Other) is set from `CollectionType` via
a lookup table, not inferred.

## Collateral consumption (`modCollateralEngine`)

```
CollateralConsumptionMonths = GetSystemParameter("COLLATERAL_CONSUMPTION_MONTHS")  ' default 60
MonthlyConsumption = OriginalEligibleCollateral / CollateralConsumptionMonths
MonthsSinceDefault = DateDiff("m", Account.DefaultDate, SnapshotPeriod)
ConsumedToDate = MIN(MonthsSinceDefault, CollateralConsumptionMonths) * MonthlyConsumption
RemainingValue = OriginalEligibleCollateral - ConsumedToDate
UncoveredExposure = MAX(0, Exposure - Provision - SuspendedInterest - RemainingValue)
```

- Anchored to `Account.DefaultDate`, **never** `SystemEntryDate`.
- `CollateralConsumptionMonths` and the eligibility flag come from
  `tbl_SystemParameter` / `tbl_Collateral.EligibleForConsumption` — fully
  configurable, never a VBA literal.
- Runs once per account per collateral per month during month-end close,
  writing one row per period to `tbl_CollateralConsumption` (append-only,
  so historical coverage at any past month-end is always reconstructible).

## Off-Balance rules (`modOffBalanceRulesEngine`)

**3-year eligibility screen** — `Account.DefaultDate` age ≥
`OFFBALANCE_3YEAR` threshold (system parameter, default 3 years) triggers:

```
EligibilityChecklist:
  ProvisionCoverage      >= threshold from tbl_RegulatoryRules('PROVISION_COVERAGE_MIN')
  SuspendedInterestCovered per rule
  LegalReviewCompleted   (from tbl_LegalCase / manual flag)
  CollateralReviewCompleted
  RecoveryPotentialAssessed
  SettlementStatus review
  RequiredApproval obtained (tbl_Approval)
```
Result is surfaced as `Eligible` / `Not Eligible` per account, feeding the
regulatory dashboard — it does **not** auto-execute an off-balance
transaction; §40 of the spec is explicit that strategy suggestions are
advisory, execution stays a human, approved action.

**6-year mandatory escalation** — age ≥ `OFFBALANCE_6YEAR` threshold
(default 6 years) and the account is still on-balance:

```
If Not Exists(tbl_RegulatoryException WHERE AccountID = X AND RuleID = 6yr AND Status <> 'Cleared') Then
    Insert tbl_RegulatoryException (
        ReasonCode, Justification, SupportingDocumentRef,
        ExpectedRecovery, ExpectedRecoveryDate,
        ResponsibleEmployeeID, ApprovalID, NextReviewDate,
        Status = 'Open'
    )
End If
```
The exception is **mandatory** — the account cannot appear "clean" on the
regulatory dashboard past 6 years without one open (or cleared-with-reason)
exception row.

**Exception aging** (`modOffBalanceRulesEngine.ExceptionAgingBucket`):
`0–30 / 31–90 / 91–180 / 181–365 / >365` days since the exception's
`NextReviewDate` was due (or `TriggerDate` if never reviewed).

## Provision & Suspended Interest bridges (`modProvisionSuspendedInterestEngine`)

Both follow the same bridge shape, one row per account per month:

```
Closing = Opening + Increase/New - Decrease/Recovery - Utilization - Waiver - Reversal
```

**Profit vs Saving classification** (§23): when a reversal/release is
recognized, compare the *originating* snapshot period of that
provision/SI balance:

```
If OriginatingPeriod <= DateSerial(Year(OriginatingPeriod), 12, 31) 
   And OriginatingPeriod's year < Year(CurrentClosingPeriod) Then
    ReleaseClassification = "Profit"      ' balance existed at prior Dec-31
Else
    ReleaseClassification = "Saving"      ' formed and reversed within current year
End If
```
Both components are always stored separately (`ReleaseClassification`
column) — never pre-summed into one number, per your explicit instruction
not to merge them silently.

## Approval workflow (`modApprovalWorkflowEngine`) — maker-checker

```
Draft -> Submit -> ManagerReview -> {Approved -> Posted | Rejected}
```

- `RequestedByEmployeeID` and `ReviewedByEmployeeID` must differ — enforced
  in code (`Err.Raise` if a manager tries to approve their own request),
  not just by UI convention.
- `RequiredApprovalLevel` is resolved from `tbl_RegulatoryRules`
  (amount/entity-type thresholds), so who *must* approve is configurable.
- Only `Posted` transactions affect `tbl_Movement` / balances; `Rejected`
  ones are kept (not deleted) for audit visibility with the reason.

## Audit trail (`modAuditTrail`) — reversal, not deletion

- Every insert/update across financially-impactful tables logs Old/New
  value pairs to `tbl_AuditTrail`.
- `modAuditTrail.AssertNotPosted` is called before any `UPDATE`/`DELETE`
  attempt on a row whose `ApprovalStatus = 'Posted'`; it raises an error
  instead of allowing the write. The only path to "undo" a posted entry is
  `modApprovalWorkflowEngine.CreateReversal`, which inserts an equal-and-
  opposite entry referencing the original via `SourceReferenceID`.

## Month-end closing (`modMonthEndClosingEngine`) — the 14 steps

Each step writes a `tbl_MonthClosing` row (`Pending → Running → Completed/
Failed`) so a failed run can be diagnosed and resumed step-by-step, not
re-run blind:

1. Import Validation 2. Data Quality 3. Reconciliation 4. Movement
Calculation 5. Recovery Calculation 6. Provision Calculation
7. Suspended Interest 8. Collateral Consumption 9. Financial Impact
10. Exception Review 11. Snapshot (append to `tbl_MonthlySnapshot`)
12. Management Reports (refresh `tbl_KPIResult`) 13. Regulatory Reports
14. Period Lock (`tbl_PeriodLock` — blocks further writes to that period
    outside `modApprovalWorkflowEngine.CreateReversal`).

## Regulatory Rules Engine (`modRegulatoryRulesEngine`)

Single access point: `GetActiveRule(RuleCode, AsOfDate)` and
`GetSystemParameter(ParameterCode, AsOfDate)` — both resolve the row with
`EffectiveDate <= AsOfDate < ExpiryDate (or Null)`, so a threshold change
(e.g. CBJ moves the off-balance window from 3 to 4 years) is a new config
row, not a code deployment, and old snapshots keep reflecting the rule that
was in force when they were computed.
