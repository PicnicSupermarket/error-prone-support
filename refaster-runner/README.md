# Refaster runner

This module exposes [Refaster][refaster] rules from the classpath as an [Error
Prone][error-prone] `BugChecker`.

## What does this module do?

The `Refaster` bug checker scans the classpath for `.refaster` files,
deserializes them into `CodeTransformer` instances, and applies them during
compilation. When multiple rules propose overlapping replacements for the same
region of code, a conflict-resolution step selects a single match.

Two compiler flags tune this behavior:

- `-XepOpt:Refaster:NamePattern=<regex>` restricts which rules are loaded,
  based on their fully qualified name.
- `-XepOpt:Refaster:DisableOptimizedRefaster=true` disables the optimized
  rule-selection algorithm described below; all loaded rules are then applied
  to every compilation unit.

## Optimized rule selection

Applying every Refaster rule to every source file scales poorly as the rule
set grows. To avoid that, each rule's required identifiers (method names,
operators, and similar syntactic markers) are extracted at load time and
indexed in a prefix tree. For each compilation unit, identifiers are extracted
from the source and used to traverse the tree; only rules reachable given
those identifiers are forwarded to the matcher.

Given three rules with identifier sets `R1 = [A, B, C]`, `R2 = [B]`, and `R3 =
[B, D]`, the resulting tree looks as follows:

```
<root>
 +-- A
 |   +-- B
 |       +-- C -- R1
 +-- B         -- R2
     +-- D     -- R3
```

A source file whose identifiers are `{B, D}` only reaches `R2` and `R3`; `R1`
is pruned without being applied. See `RefasterRuleSelector` for the entry
point and further details.

[error-prone]: https://errorprone.info
[refaster]: https://errorprone.info/docs/refaster
