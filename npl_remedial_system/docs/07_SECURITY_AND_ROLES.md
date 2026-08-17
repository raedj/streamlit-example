# 07 — Security & Roles

## Roles (`tbl_UserRole` / `tbl_Employee.RoleCode`)

| Role | Can view | Can do | Cannot |
|---|---|---|---|
| **Employee** | Own assigned accounts (`tbl_Assignment` / `tbl_Account.AssignedEmployeeID`) | Add notes/follow-ups/promises, submit collection/settlement/adjustment as Draft | Approve own or any submission; edit posted records; access other officers' portfolios (row-level via query `WHERE AssignedEmployeeID = CurrentEmployeeID()`) |
| **Team Manager** | Own team's accounts | Approve team submissions (up to `RequiredApprovalLevel='TeamManager'`), manage team assignment, view team performance | Approve their own submissions; approve above their threshold |
| **Department Manager** | Full department portfolio | Approve high-value/escalated submissions, manage portfolio-level exceptions, review 6-year escalations | Bypass maker-checker; edit `tbl_RegulatoryRules` |
| **Admin** | Everything | Configuration (`tbl_SystemParameter`, `tbl_RegulatoryRules`, `tbl_ExcelTemplate`), user management, run imports/backups | Approve financial transactions (segregation of duties — Admin is a system role, not a credit-decision role) |
| **Audit / Read-Only** | Everything, including `tbl_AuditTrail` | Run reports | Any write, anywhere |

## Enforcement mechanism

1. **Row-level filtering**: every list form's `RecordSource` includes a
   `WHERE` clause driven by `CurrentEmployeeID()` and role, not client-side
   hiding — Employees' queries never select rows outside their assignment.
2. **Action-level checks in VBA, not just hidden buttons**: both
   `modApprovalWorkflowEngine.ReviewApproval` (self-approval block) and
   `modAuditTrail.AssertNotPosted` (no editing posted rows) run
   server-side (i.e., in the shared back-end logic every front-end calls),
   so a modified front-end copy or a direct table edit attempt still
   fails.
3. **Access-native security**: back-end `.accdb` opened only via the
   front-end's linked-table connection string; back-end file share
   permissions (NTFS) restrict direct file access to the service
   account/Admins only — end users only ever go through the front-end.
4. **`WindowsLogin`** on `tbl_Employee` ties every audit entry and every
   approval to a real AD identity, resolved via `Environ("USERNAME")` —
   never a manually-typed "logged in as" field.

## Segregation of duties matrix (maker-checker)

| Entity | Maker | Checker (minimum) |
|---|---|---|
| Manual Collection | Employee | Team Manager |
| Waiver | Employee | Team Manager or Dept Manager (per `tbl_RegulatoryRules` amount threshold) |
| Settlement | Employee | Team Manager |
| Write-off | Employee/Team Manager | Department Manager |
| Off-Balance | Team Manager | Department Manager |
| Reversal/Adjustment | Requesting employee | Any manager other than the requester |
| Regulatory Rule change | Admin | (logged; recommend a second-Admin review process outside system scope) |

## Data privacy

`tbl_Customer.CustomerReference` is documented as masked/tokenized per the
bank's National-ID handling policy — this system stores whatever
reference the bank's policy allows, never raw national ID numbers unless
explicitly sanctioned; enforcement of the masking itself is a bank-policy
decision made before data enters `Incoming\`, not something this schema
can verify.
