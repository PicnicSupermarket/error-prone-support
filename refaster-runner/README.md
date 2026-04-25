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

Applying every loaded Refaster rule to every source file scales poorly:
the matcher work grows roughly as `rules * files`. Instead, each rule
declares the identifiers it requires, and the checker only runs a rule
when those identifiers appear in the compilation unit under
consideration. The filter is sound by construction: a rule is skipped
only when it provably cannot fire, so results match those of the
unoptimized runner (see the `DisableOptimizedRefaster` flag above to
turn the optimization off).

### How it works

The algorithm runs in three steps.

1. **Indexing rules at load time**
   (`RefasterRuleIdentifierExtractor`, `Node.Builder`).
   - For each `RefasterRule`, walk every `@BeforeTemplate` and collect
     the identifiers it references. "Identifier" covers method names,
     simple class names, member-reference targets (e.g. `String::isEmpty`
     contributes `isEmpty` and `String`), static-field names, and the
     operator or assignment symbols (`&&`, `==`, `+=`, ...) produced by
     `TreeKindStringifier`.
   - `Refaster.anyOf(a, b, c)` is treated as a disjunction: the
     extractor emits one identifier set per branch, so a single rule
     may occupy multiple paths in the tree. Reaching any one of them
     during traversal suffices to select the rule.
   - Each set is sorted lexicographically and inserted into an
     immutable prefix tree (`Node`). Within a rule, shorter paths are
     inserted first, so a longer path is dropped when a strict prefix
     already leads to the same rule.
2. **Extracting identifiers from the source file**
   (`SourceIdentifierExtractor`).
   - For each compilation unit, the extractor scans the abstract
     syntax tree and collects the same categories of identifiers used
     in step 1.
   - Several constructs are deliberately excluded so the candidate set
     stays sound and sharp: package declarations, class headers (only
     members are scanned), generated constructors, and variable and
     parameter names (Refaster templates never bind on user-chosen
     local names).
3. **Traversing the tree** (`Node.collectReachableValues`).
   - The source identifiers are sorted lexicographically and used as
     edge labels in a depth-first traversal of the tree.
   - Using the same order on both sides keeps the walk single-pass:
     once an edge cannot be matched against the (remaining) source
     identifiers, every rule deeper than that edge is already out of
     reach, so no backtracking is needed.
   - Any value attached to a visited node is a candidate rule:
     reaching the node is itself proof that every identifier on the
     path from the root is present in the source.
   - At each step the traversal iterates whichever of `children` and
     remaining candidate edges is smaller, keeping the walk cheap when
     either side is sparse.

### Worked example

Take three rules with identifier sets `R1 = [A, B, C]`, `R2 = [B]`, and
`R3 = [B, D]`. Step 1 produces the following tree:

```
<root>
 +-- A
 |   +-- B
 |       +-- C -- R1
 +-- B         -- R2
     +-- D     -- R3
```

Now suppose step 2 extracts identifiers `{B, D}` from some source file.
Step 3 descends from the root along `B`, reaches `R2`, then follows `D`
to reach `R3`. The `A`-rooted subtree is never entered because no edge
`A` is among the source identifiers, so `R1` is pruned without being
applied.

Selection is the only change introduced by the optimization: the
candidate rules are then matched and fixed exactly as in the
unoptimized path, and overlapping replacements are resolved by
`Refaster#applyMatches` as usual. See `RefasterRuleSelector` for the
entry point.

[error-prone]: https://errorprone.info
[refaster]: https://errorprone.info/docs/refaster
