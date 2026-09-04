# fint-kontroll-user-catalog

## User Status Derivation

The catalog owns the final user status. Incoming factory users provide source
facts only:

- `fintStatus=VALID`: FINT has enough source data to maintain the user.
- `fintStatus=INVALID`: FINT is missing a required source relationship.
- `fintStatus=ACTIVE|DISABLED`: legacy factory values accepted temporarily during
  rollout only.
- `entraStatus=ACTIVE|DISABLED`: Entra account state.
- `validFrom` and `validTo`: the FINT validity window.

Final status is derived in this order:

- Tombstone records mark existing users as `DELETED`.
- `fintStatus=INVALID` or an unknown FINT status becomes `INVALID`.
- Legacy `fintStatus=ACTIVE` is handled like `VALID` during rollout.
- Legacy `fintStatus=DISABLED` becomes `DISABLED` during rollout.
- `entraStatus=DISABLED` becomes `DISABLED`.
- `entraStatus=ACTIVE` plus `fintStatus=VALID` becomes `ACTIVE` only when the
  current time is inside the validity window, including the configured
  days-before-start allowance.
- Any other combination becomes `DISABLED`.

TODO FKS-1648: Remove the legacy `fintStatus=ACTIVE|DISABLED` bridge after all
factory instances publish `VALID|INVALID` and old Kafka records can no longer be
replayed.

When an existing catalog user receives `INVALID` or a tombstone, only `status`
and `statusChanged` are updated. Existing identity, name, username, and org data
are kept because invalid payloads from the factory may be intentionally minimal.
New users are created only for derived `ACTIVE` or `DISABLED` statuses.
