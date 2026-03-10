---
applyTo: "**/refasterrules/**"
---

# Refaster Rules: Conventions and Step-by-Step Guide

This document describes the conventions for creating and modifying Refaster
rules in this project. It serves as the canonical reference for all AI coding
agents and human contributors.

For general project context, see [CONTRIBUTING.md][contributing].

## Overview

[Refaster][refaster] rules define source code transformations using
`@BeforeTemplate` / `@AfterTemplate` method pairs. Each `@BeforeTemplate`
matches a code pattern; the `@AfterTemplate` specifies its replacement. Rules
are grouped into topic-based _collections_ (e.g., `BigDecimalRules`).

## File locations

| Purpose | Path |
|---------|------|
| Rule source | `error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/{Topic}Rules.java` |
| Test input | `error-prone-contrib/src/test/resources/tech/picnic/errorprone/refasterrules/{Topic}RulesTestInput.java` |
| Test output | `error-prone-contrib/src/test/resources/tech/picnic/errorprone/refasterrules/{Topic}RulesTestOutput.java` |
| Test registration | `error-prone-contrib/src/test/java/tech/picnic/errorprone/refasterrules/RefasterRulesTest.java` |

## Step 1 - Create the rule file

Create (or extend) `{Topic}Rules.java`. A new collection looks like this:

```java
package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Topic}s. */
@OnlineDocumentation
final class {Topic}Rules {
  private {Topic}Rules() {}

  /** Prefer X over Y. */
  static final class {RuleName} {
    @BeforeTemplate
    SomeType before(SomeType value) {
      return value.worseAlternative();
    }

    @AfterTemplate
    SomeType after(SomeType value) {
      return value.preferredAlternative();
    }
  }
}
```

Conventions:
- The outer class is `final`, annotated with `@OnlineDocumentation`, and has a
  `private` constructor.
- The outer class's Javadoc follows one of these patterns:
  - For AssertJ assertion collections: `Refaster rules related to AssertJ
    assertions over {topic}s.` (use the concrete subject type, e.g. `{@link
    BigDecimal}` or `{@code int}`, not an abstract class such as
    `AbstractBigDecimalAssert` or `AbstractIntegerAssert`).
  - For migration collections (annotated with `@TypeMigration`): `Refaster
    rules that replace {SourceLibrary} APIs with {TargetLibrary} equivalents.
  - For other rule collections: `Refaster rules related to expressions dealing
    with {@link Topic}s.`
- Each rule is a `static final class` nested inside the outer class.
- Every rule has a Javadoc comment of the form `/** Prefer X over Y. */`, where
  `X` is generally a Javadoc link ({@link ...}) to the preferred alternative,
  and `Y` is a qualifier describing the before-template code. See [Javadoc
  conventions](#javadoc-conventions) below for how to choose the right
  qualifier.
- Use `{@link ClassName#method(ArgumentTypes)}` or `{@link ClassName#CONSTANT}`
  for after-template expressions with a single method invocation or field
  dereference. Use `{@code expression}` in other cases, including method
  reference usages.
- `@BeforeTemplate` methods are named `before`. Use `before2`, `before3`, etc.
  only when disambiguation is required (i.e., when multiple `@BeforeTemplate`
  methods have identical parameter types and would otherwise be ambiguous).
- The `@AfterTemplate` method is named `after`.
- Method parameters are generally listed in the order in which they first occur
  in the `@AfterTemplate` method.

### Javadoc conventions

If applicable, prefer one of the following qualifier variants (or combinations
thereof) in the Javadoc comment:

- "Prefer X over deprecated alternatives."
- "Prefer X over imprecisely typed alternatives."
- "Prefer X over less efficient alternatives."
- "Prefer X over less explicit alternatives."
- "Prefer X over less idiomatic alternatives."
- "Prefer X over less secure alternatives."
- "Prefer X over more contrived alternatives."
- "Prefer X over more fragile alternatives."
- "Prefer X over more verbose alternatives."
- "Prefer X over non-AssertJ alternatives."
- "Prefer X over non-JDK alternatives."
- "Prefer X over the associated constructor."

Use the qualifier that best describes _why_ the `@AfterTemplate` is preferred.
Do **not** copy the qualifier from a nearby rule; select it independently based
on the table below:

| Qualifier | When to use |
|-----------|-------------|
| "deprecated" | The before-template uses an API annotated `@Deprecated` |
| "imprecisely typed" | The before-template returns a broader type that does not communicate a key property (such as immutability, sortedness, or subtype specificity) that the after-template's return type does. For example, `Map.of()` returns `Map` while `ImmutableMap.of()` returns `ImmutableMap`, communicating immutability at the type level |
| "less efficient" | The after-template avoids unnecessary work that is evident from inspecting the code (no benchmarks required). Look for: (1) unnecessary object allocations — e.g., `str.substring(i).indexOf(ch)` allocates a new string vs. `str.indexOf(ch, i)` which does not; (2) unnecessary intermediate data structures — e.g., materializing a List only to stream it; (3) unnecessary serialization/deserialization round-trips; (4) worse algorithmic complexity — e.g., sorting O(n log n) to find a min/max that can be found in O(n); (5) using a general API when a specialized, allocation-free alternative exists — e.g., `str.trim().isEmpty()` vs. `str.isBlank()`, or `BigDecimal.valueOf(0)` vs. the cached `BigDecimal.ZERO`. If in doubt, inspect the implementation of invoked methods |
| "less explicit" | The after-template conveys the programmer's intent more clearly. |
| "less idiomatic" | A well-known Java or library idiom exists that directly expresses the intent |
| "less secure" | The before-template creates resources or performs operations with weaker security properties (such as more liberal file permissions) than the after-template |
| "more contrived" | The before-template takes a needlessly roundabout approach (e.g., converting to a stream and back) **and** you have verified that none of the other categories in this list apply |
| "more fragile" | The before-template's behavior depends on preconditions (such as non-null intermediate values, specific evaluation order, or exact return value contracts) that may not hold in all contexts, while the after-template handles those edge cases gracefully or does not depend on those preconditions |
| "more verbose" | The before-template uses more code to express the same thing; the only issue is length |
| "non-AssertJ" | The before-template uses a non-AssertJ assertion library (e.g., JUnit, TestNG) when an AssertJ equivalent exists |
| "non-JDK" | The before-template uses a third-party library call when a JDK equivalent exists |
| "the associated constructor" | The before-template uses `new X(...)` when a static factory method exists |

When multiple qualifiers apply (e.g. in case of multiple `@BeforeTemplate`
methods or `Refaster.anyOf` usage), enumerare all that apply: e.g. "Prefer X
over less efficient, more fragile or more contrived alternatives".

#### Choosing the right Javadoc qualifier

Selecting the wrong qualifier (or defaulting to "more contrived" when a more
precise one applies) produces misleading documentation. Follow this process:

1. **Inspect the before-template's operations.** For each expression, ask:
   - Does it allocate an object that the after-template avoids? (-> "less
     efficient")
   - Does it build an intermediate collection, array, or string that the
     after-template avoids? (-> "less efficient")
   - Does it perform more traversals, comparisons, or conversions than the
     after-template? (-> "less efficient")
   - Does it use an API marked `@Deprecated`? (-> "deprecated")
   - Is it a third-party call replaceable by a JDK equivalent? (-> "non-JDK")
2. **Compare with the after-template.** If the after-template calls a method
   whose implementation you can reason about (a JDK method, a well-known
   library method, or if source code is available), consider what that method
   does internally. For example, `String.isBlank()` scans characters without
   allocating, while `str.trim().isEmpty()` first allocates a trimmed copy.
3. **Reserve "more contrived" for cases with no efficiency difference.** Use it
   only when the before-template takes a roundabout path (e.g., converting to a
   stream and back) but the operations are otherwise equivalent in cost. If
   there is _any_ observable difference in allocations, complexity, or work
   performed, prefer "less efficient" — possibly combined with another
   qualifier if appropriate (e.g., "more contrived and less efficient").
   Note: redundant single-object allocations (e.g., a `Map.Entry` or a
   one-element collection) are considered negligible and do not warrant the
   "less efficient" qualifier. "More contrived" is appropriate for these cases.
4. **When in doubt, trace the operations.** List what the before-template does
   step by step (allocate X, iterate Y, copy Z) and what the after-template
   does. If the after-template's list is strictly shorter or avoids a costly
   step, "less efficient" applies.

#### Genral Javadoc comment rules

- Every rule class Javadoc **must** use the `"Prefer X over Y"` form. Rewrite
  imperative prohibitions like `"Avoid using X"` or `"Don't use X"` into this
  form.
- Identity rules (e.g. `StringIdentity`) whose `@AfterTemplate` simply returns
  a parameter as-is should use `"Prefer using {@link X}s as-is over less
  efficient alternatives."` (adjust the qualifier as appropriate).
- Use `{@link Foo}s` (appended `s`) for plural forms; **not** `{@link Foo}
  instances`.
- Developer-facing `// XXX:` comments must appear **after** the Javadoc block
  and **before** the class or method declaration; never inside Javadoc.

### How rules interact with each other

All Refaster rules in the project are applied to a codebase repeatedly, until
no more changes occur. This means that every `@BeforeTemplate` must be written
as if all other rules have already been applied. Follow these two principles:

#### Principle 1: Use already-rewritten expressions in `@BeforeTemplate`s

When writing a `@BeforeTemplate`, check whether any sub-expression in it is
matched by another existing rule. If so, use the *after-template* (rewritten)
form of that sub-expression, not the *before-template* form.

**Why:** The other rule will have already simplified that sub-expression. If
you use the original (pre-rewrite) form, your rule will never match real code.

**Example: `BigDecimalSignumIsZero` depends on `BigDecimalZero`:**

`BigDecimalZero` rewrites `BigDecimal.valueOf(0)` -> `BigDecimal.ZERO`.
Therefore, `BigDecimalSignumIsZero` must use `BigDecimal.ZERO` in its
`@BeforeTemplate`:

```java
// CORRECT: uses the already-rewritten form (`BigDecimal.ZERO`)
static final class BigDecimalSignumIsZero {
  @BeforeTemplate
  boolean before(BigDecimal value) {
    return value.compareTo(BigDecimal.ZERO) == 0;
  }
  // ...
}

// WRONG: uses the pre-rewrite form (`BigDecimal.valueOf(0)`)
static final class BigDecimalSignumIsZero {
  @BeforeTemplate
  boolean before(BigDecimal value) {
    return value.compareTo(BigDecimal.valueOf(0)) == 0;
    //                     ^^^^^^^^^^^^^^^^^^^^^
    //  BigDecimalZero already rewrites this to BigDecimal.ZERO,
    //  so this template will never match.
  }
  // ...
}
```

**Example: multi-step chain in `CollectionRules`:**

`CollectionIteratorNext` rewrites
`collection.stream().findFirst().orElseThrow()` ->
`collection.iterator().next()`. Then `SequencedCollectionGetFirst` matches
`collection.iterator().next()`; it does **not** also include
`collection.stream().findFirst().orElseThrow()`, because that form has already
been rewritten.

**Checklist:**
- For each sub-expression in your `@BeforeTemplate`, search the codebase for
  existing rules that rewrite it (look in other rules' `@AfterTemplate`
  methods).
- If found, replace the sub-expression with the other rule's `@AfterTemplate`
  output.

#### Principle 2: Check if your new rule makes existing rules redundant

When you add a new rule, check whether it makes any existing rules partially or
fully redundant:
- Search for your new rule's `@AfterTemplate` expression in other rules'
  `@BeforeTemplate` methods; those rules may now be simplifiable.
- Search for your new rule's `@BeforeTemplate` expression in other rules'
  `@AfterTemplate` methods; those rules may now be redundant.

Update or remove affected rules as needed.

#### Exception: collapsing passes

A rule _may_ include pre-rewrite sub-expressions in its `@BeforeTemplate` if
the rule is a "more specific template" for those expressions. This should only
be done if the larger match context enables a rewrite that cannot be split up
into multiple steps, either because such smaller steps are not generally
applicable, or because a multi-step rewrite would lose relevant context. In
that case, the rule must add `@SuppressWarnings("OtherRuleName" /* This is a
more specific template. */)` to prevent the other rule from matching the same
code. This allows the rule to match code that would otherwise be rewritten in
multiple passes, collapsing those passes into a single, more targeted rewrite.

### Parameter naming conventions

Template method parameters are generally derived from their type name
(e.g. `Collection` -> `collection`). Nested types are named after their inner
type (e.g. `Table.Cell` -> `cell`). In some cases we use a canonical
abbriviation (e.g. `Comparator` -> `cmp`). If a parameter has different types
between methods, name the parameter after the widest type used. If multiple
parameters have the same type, add a number suffix. However, when multiple
parameters share a type but play distinct roles, prefer semantic names over
numbered suffixes (e.g., `addTo`/`elementsToAdd` instead of
`collection1`/`collection2`). Some examples:

| Parameter type combination | Name |
|----------------------------|------|
| `Collection` | `collection` |
| `Comparator` | `cmp` |
| Fixed-arity elements | `e1`, `e2`, `e3`, ... |
| `ImmutableList.Builder` | `builder` |
| `InputStream` / `OutputStream` | `in` / `out` |
| `Iterable` and `Collection` and `Iterator` and array | `iterable` |
| `Iterable` | `iterable` |
| `Iterator` | `iterator` |
| `Map.Entry` | `entry` |
| Map key-value pairs | `k1`, `v1`, `k2`, `v2`, ... |
| `Map` / `Multimap` | `map`, `multimap` |
| `Object[]` | `array` |
| `Set` pairs | `set1`, `set2` |
| Single key-value pair | `key`, `value` |
| `Stream` | `stream` |
| `Table.Cell` | `cell` |
| `T[]` | `array` |

### Naming conventions

Rules are named after the code in their `@AfterTemplate` method, following
these guidelines:
- The name of a rule class is derived from the `@AfterTemplate` method by
  concatenating all non-variable non-`@Placeholder` identifiers, constants,
  Java keywords (such as `new` and `instanceof`), and operators (such as
  `LessThan` for `<`, `LessThanOrEqualTo` for `<=`) as CamelCase.
- Except for resolving name clashes (see below), **there are no exceptions to
  the preceding rule**; do not use more semantic names.
- In case of instance method or field references, the type of the dereferenced
  parameter is also included.
- Rule names must be unique within the same class, but need not be unique
  across different classes. Do not add disambiguation suffixes for names that
  only coincide with rules in other classes.
- In case of arity-based name clashes, rename new or existing rules as
  necessary to disambiguate them:
  - Variants with more template methods parameters are amended to include
    `WithX` or `WithXAndY`, where `X and `Y` are the additional parameter
    type(s). If the additional paramater type is `@Repeated`, use
    `WithVarargs`.
  - In case there's an expression (non-`void` returning) and block (`void`
    returning) variant, append `Expression` and `Block` as appropriate).
  - When rules differ only in the number of arguments (arity), append a numeric
    suffix: e.g., `ImmutableMapOf1`, `ImmutableMapOf2`, etc.
- When invoking static methods or fields, check the rest of the code base to
  assess whether a method or field is usually statically imported; if so,
  follow that style.
- When the `@AfterTemplate` simply returns a parameter as-is (not dereferenced
  or otherwise operated on), use `{TypeName}Identity`, where `{TypeName}` is a
  descriptive name for the parameter type (e.g., `ComparatorIdentity` for a
  rule that returns a `Comparator` parameter).

When the derived name is very long, still follow the convention. For instance,
a rule whose after-template is `ImmutableTable.of(cell.getRowKey(),
cell.getColumnKey(), cell.getValue())` is named
`ImmutableTableOfCellGetRowKeyCellGetColumnKeyCellGetValue`. Do not abbreviate
or use input-pattern-based names (e.g., do not use `CellToImmutableTable`). If
the type prefix on an instance method is unambiguous from context (e.g., `Cell`
instead of `Table.Cell`), the shorter form may be used (as shown).

Examples:

| Code | Name |
|------------|------|
| `return BigDecimal.ZERO` | `BigDecimalZero;` |
| `return ImmutableList.sortedCopyOf(collection).iterator();` | `ImmutableListSortedCopyOfIterator` |
| `return ImmutableList.sortedCopyOf(cmp, collection).iterator();` | `ImmutableListSortedCopyOfIterator` or `ImmutableListSortedCopyOfIteratorWithComparator` |
| `return addTo.addAll(elementsToAdd);` | `CollectionAddAll` or `CollectionAddAllExpression` |
| `addTo.addAll(elementsToAdd);` | `CollectionAddAll` or `CollectionAddAllBlock` |
| `return stream.collect(toImmutableMap(e -> keyFunction(e), e -> valueFunction(e))); | `StreamCollectToImmutableMap` |
| `return new ArrayList<>(collection);` | `NewArrayList` |
| `return value1.compareTo(value2) < 0;` | `StringCompareToLessThanZero` or `EnumCompareToLessThanZero` |
| `return cmp;` | `ComparatorIdentity` |

### Real example: `BigDecimalRules.BigDecimalZero`

```java
/** Prefer {@link BigDecimal#ZERO} over less efficient alternatives. */
static final class BigDecimalZero {
  @BeforeTemplate
  BigDecimal before() {
    return Refaster.anyOf(BigDecimal.valueOf(0), new BigDecimal("0"));
  }

  @AfterTemplate
  BigDecimal after() {
    return BigDecimal.ZERO;
  }
}
```

### `@SuppressWarnings` on template methods

When a template method triggers a compiler or static-analysis warning, it's
generally okay to suppress it with a comment. Preferably, use an established
comment text. For example, if the warning can be suppressed using key
`java:S2111`, then first search for existing references:

```sh
git grep -B6 '"java:S2111"' -- '*.java' | grep -A6 "@BeforeTemplate\|@AfterTemplate" | grep -A5 '@SuppressWarnings.*'
```

And if found, use the same format:

```java
@BeforeTemplate
@SuppressWarnings("java:S2111" /* This violation will be rewritten. */)
BigDecimal before(double value) {
  return new BigDecimal(value);
}
```

Established comment patterns:

| Comment | When used |
|---------|-----------|
| `/* This violation will be rewritten. */` | The flagged code is inside a before-template pattern that will be replaced, and the `after-template` code is not similarly flagged |
| `/* This deprecated API usage will be rewritten. */` | Variant for `"deprecation"` suppressions |
| `/* This is a more specific template. */` | Suppressing another Refaster rule that would also match |
| `/* Safe generic array type creation. */` | Suppressing `"unchecked"` for generic array creation |
| `/* Cast is presumed safe in matched context. */` | Suppressing `"unchecked"` for casts |
| `/* Parentheses compensate for a Refaster bug. */` | Suppressing `"UnnecessaryParentheses"` |
| `/* SonarCloud thinks that \`someParamater\` itself is \`@Nullable\`. */` | Suppressing SonarCloud false positive on `Supplier<@Nullable String>` parameters |
| `/* Each variant requires a separate \`@BeforeTemplate\` method. */` | Rule class exceeds method-count threshold due to per-type overloads |

Every `@SuppressWarnings` entry must have a `/* ... */` comment explaining why
the suppression is necessary. If multiple suppression entries are required, add
an additional
`"z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"`
entry at the end of the list. This self-documenting entry is the only entry
that must *not* come with a comment.

Note: `@SuppressWarnings` may appear on the rule class (not just template
methods) when a warning applies structurally to the whole rule.

The requirement that every `@SuppressWarnings` entry must have a `/* ... */`
comment applies to all Java files in the project, including test resource
files.

### Behavior-changing warnings in Javadoc

When a rule changes observable behavior (e.g., throws an exception instead of
silently deduplicating, or changes iteration order), add a
`<p><strong>Warning:</strong>` paragraph to the Javadoc. This is distinct from
`// XXX:` comments, which are developer-facing notes. The warning paragraph is
user-facing and will appear in the generated documentation.

Example:
```java
/**
 * Prefer {@link Sets#toImmutableEnumSet()} over less efficient alternatives.
 *
 * <p><strong>Warning:</strong> this rewrite changes the iteration order of
 * the resulting set.
 */
```

### `// XXX:` comments

Use class- or method-level `// XXX:` comments to document:
- Known limitations or behavioral differences between the before- and
  after-template (e.g., different exception types, changed null handling, or
  loss of deduplication) that are too minor to be called out in Javadoc.
- Future improvement ideas or Refaster engine limitations.
- References to related rules or checks that may overlap.

### Method and type parameter usage

Where applicable, make sure that method and type parameters are as wide as
possible.

For example, when matching invocations of `Stream<T>#forEach(Consumer<? super
T> action)`, do *not* write:

```java
static final class CollectionForEach<T> {
  @BeforeTemplate
  void before(Collection<T> collection, Consumer<? super T> consumer) {
    collection.stream().forEach(consumer);
  }

  @AfterTemplate
  void after(Collection<T> collection, Consumer<? super T> consumer) {
    collection.forEach(consumer);
  }
}
```

Instead write:
```java
static final class CollectionForEach<S, T extends S> {
  @BeforeTemplate
  void before(Collection<T> collection, Consumer<S> consumer) {
    collection.stream().forEach(consumer);
  }

  @AfterTemplate
  void after(Collection<T> collection, Consumer<S> consumer) {
    collection.forEach(consumer);
  }
}
```

Template method parameters and `@Placeholder` methods should avoid wildcard
bounds where possible. Thus, do *not* write this:

```java
static final class ComparatorThenComparing<S, T extends Comparable<? super T>> {
  @BeforeTemplate
  Comparator<S> before(Comparator<S> cmp, Function<? super T, ? extends T> function) {
    return cmp.thenComparing(comparing(function));
  }

  @AfterTemplate
  Comparator<S> after(Comparator<S> cmp, Function<? super T, ? extends T> function) {
    return cmp.thenComparing(function);
  }
}
```

Instead write:
```java
static final class ComparatorThenComparing<R, S extends R, T extends Comparable<? super T>, U extends T> {
  @BeforeTemplate
  Comparator<S> before(Comparator<S> cmp, Function<R, U> function) {
    return cmp.thenComparing(comparing(function));
  }

  @AfterTemplate
  Comparator<S> after(Comparator<S> cmp, Function<R, U> function) {
    return cmp.thenComparing(function);
  }
}
```

This way before- and after-template method parameters are provably compatible.

When eliminating a wildcard from a method parameter is not possible in Java's
type system, add a `// XXX:` comment explaining why the wildcard cannot be
eliminated.

### Use the most specific return type

Method return types do not influence Refaster matching, but do aid in rule
understanding and review. Thus, always declare the most specific return type
possible.

## Step 2 - Advanced patterns

### `Refaster.anyOf(...)` for multiple before-patterns

Match several syntactically distinct but otherwise equivalent patterns in a
single `@BeforeTemplate`:

```java
@BeforeTemplate
BigDecimal before() {
  return Refaster.anyOf(BigDecimal.valueOf(0), new BigDecimal("0"));
}
```

`Refaster.anyOf()` calls can be nested to express combinations of alternatives:

```java
@BeforeTemplate
Optional<T> before(Queue<T> queue) {
  return queue.isEmpty()
      ? Optional.empty()
      : Refaster.anyOf(Optional.of(queue.peek()), Optional.ofNullable(queue.peek()));
}
```

### Prefer `Refaster.anyOf` over multiple `@BeforeTemplate` methods

When several before-patterns differ only in a sub-expression but share the same
overall structure and parameter types, use `Refaster.anyOf` inside a single
`@BeforeTemplate` method rather than defining separate `@BeforeTemplate`
overloads. This keeps rules concise and makes it immediately clear which parts
of the expression vary.

Multiple `@BeforeTemplate` methods are still necessary when the alternatives:
- have **different parameter types** (e.g., `Collection<T>` vs.
  `ImmutableCollection<T>` vs. `SetView<T>`);
- are **structurally incompatible** (e.g., a `forEach` call vs. an enhanced
  `for`-loop) due to Refaster engine limitations; or
- are **void-returning statements** whose entire invocation structure differs
  (e.g., `list.addLast(e);` vs. `list.add(list.size(), e);`).

In short: if two before-patterns can be expressed as a single template with one
or more `Refaster.anyOf` calls substituted for the varying sub-expressions,
they should be. Reserve separate `@BeforeTemplate` methods for cases where this
is not possible.

### `@AlsoNegation` for auto-negated variants

When `@AlsoNegation` is applied to an `@AfterTemplate`, Refaster automatically
generates a negated variant (e.g., `a == 0` also matches `a != 0`, and
`x.isEmpty() ? a : b` also matches `!x.isEmpty() ? b : a`):

```java
@AfterTemplate
@AlsoNegation
boolean after(BigDecimal value) {
  return value.signum() == 0;
}
```

Use `@AlsoNegation` when the negated form is simply the logical negation of the
`@AfterTemplate` expression (i.e., prepending `!`). Use **separate rules** when
the positive and negative cases have structurally different preferred
expressions (e.g., `optional.isEmpty()` vs. `optional.isPresent()`).

### `@Matches` / `@NotMatches` for parameter constraints

Constrain which expressions a parameter may match using matchers
from `tech.picnic.errorprone.refaster.matchers`:

```java
// Note: `S` and `U` are class-level type parameters declared as
// `<S, T extends Comparable<? super T>, U extends T>`.
@BeforeTemplate
Comparator<T> before(
    @Matches(IsIdentityOperation.class)
        Function<S, U> keyExtractor) {
  return comparing(keyExtractor);
}
```

```java
@BeforeTemplate
List<T> before(@NotMatches(IsRefasterAsVarargs.class) T[] array) {
  return Arrays.stream(array).toList();
}
```

### `@Placeholder` with abstract classes

Use `@Placeholder` methods to parameterize rule behavior. In this case the rule
class must be `abstract static` (not `static final`). These abstract classes
will not actually be extended (Refaster is a custom DSL), so wildcard bound
elimination is safe to apply to these classes.

```java
abstract static class StreamCollectToImmutableMap<E, K, V> {
  @Placeholder(allowsIdentity = true)
  abstract K keyFunction(@MayOptionallyUse E element);

  @Placeholder(allowsIdentity = true)
  abstract V valueFunction(@MayOptionallyUse E element);

  @BeforeTemplate
  ImmutableMap<K, V> before(Stream<E> stream) {
    return stream
        .map(e -> Map.entry(keyFunction(e), valueFunction(e)))
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @AfterTemplate
  @UseImportPolicy(STATIC_IMPORT_ALWAYS)
  ImmutableMap<K, V> after(Stream<E> stream) {
    return stream.collect(
        toImmutableMap(e -> keyFunction(e), e -> valueFunction(e)));
  }
}
```

### `@Repeated` with `Refaster.asVarargs()`

Match varargs parameters:

```java
static final class CollectionsMinArraysAsList<S, T extends S> {
  @BeforeTemplate
  T before(@Repeated T value, Comparator<S> cmp) {
    return Stream.of(Refaster.asVarargs(value)).min(cmp).orElseThrow();
  }

  @AfterTemplate
  T after(@Repeated T value, Comparator<S> cmp) {
    return Collections.min(Arrays.asList(Refaster.asVarargs(value)), cmp);
  }
}
```

When using `@Repeated` parameters, **always** wrap them with
`Refaster.asVarargs(param)` in both `@BeforeTemplate` and `@AfterTemplate`
methods.

### `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`

Force the replacement to use a static import:

```java
@AfterTemplate
@UseImportPolicy(STATIC_IMPORT_ALWAYS)
Comparator<T> after() {
  return naturalOrder();
}
```

This annotation may be intentionally omitted when a static import would clash
with an existing import from the before-template's library. An `// XXX:`
comment must explain the omission in such cases.

### Multiple `@BeforeTemplate` overloads

When a rule matches structurally different patterns, define multiple
`@BeforeTemplate` methods named `before`, `before2`, `before3`, etc.:

```java
static final class CollectionAddAllBlock<T, S extends T> {
  @BeforeTemplate
  void before(Collection<T> addTo, Collection<S> elementsToAdd) {
    elementsToAdd.forEach(addTo::add);
  }

  @BeforeTemplate
  void before2(Collection<T> addTo, Collection<S> elementsToAdd) {
    for (T element : elementsToAdd) {
      addTo.add(element);
    }
  }

  @AfterTemplate
  void after(Collection<T> addTo, Collection<S> elementsToAdd) {
    addTo.addAll(elementsToAdd);
  }
}
```

When a rule matches the same conceptual pattern across multiple parameter types
(e.g., `T[]`, `Iterator<T>`, `Iterable<T>`, `Collection<T>`), declare
`@BeforeTemplate` methods in order from widest to narrowest.

### `@Description` and `@Severity`

Override the default description or severity for a rule:

```java
@Description(
    "From Reactor 3.5.0 onwards, `take(n)` no longer requests "
        + "an unbounded number of elements upstream.")
static final class FluxTake<T> {
  // ...
}
```

```java
@Severity(SeverityLevel.ERROR)
static final class SomeStrictRule {
  // ...
}
```

## Step 3 - Create the test input file

Create `{Topic}RulesTestInput.java` in
`src/test/resources/tech/picnic/errorprone/refasterrules/`:

```java
package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class {Topic}RulesTest implements RefasterRuleCollectionTestCase {
  SomeType test{RuleName}() {
    return value.worseAlternative();
  }
}
```

Conventions:
- The class is named `{Topic}RulesTest` and implements
  `RefasterRuleCollectionTestCase`.
- Each inner rule class `FooBar` gets a test method named `testFooBar()`.
- The test method does not accept parameters.
- The test method body contains the _before_ code (the code that should be
  matched and rewritten).
- Test method order must match rule declaration order.
- Make sure that all variants of before-templates are matched, considering also
  all `Refaster.anyOf` variants and variants implied by `@AlsoNegation`.
- Similarly, as Refaster automatically generates template variants for negated
  ternary conditions and `if`-statements (even without `@AlsoNegation`), any
  rule with a ternary expression or `if`-statement in its `@BeforeTemplate`
  should include both the canonical and negated forms in tests: e.g.
  `condition ? a : b` and `!condition ? b : a`.
- When a rule uses `@AlsoNegation`, include negated variants only if the
  before- or after-template would structurally change under negation (e.g. `<`
  -> `>=`). If the after-template is unchanged (e.g. in case of
  `collection.isEmpty()`), the negated form can be omitted.
- Test cases must appear in the order of the rule's `@BeforeTemplate` methods;
  within each `@BeforeTemplate`, list `Refaster.anyOf` alternatives in
  declaration order. For nested `Refaster.anyOf` expressions, enumerate test
  cases outer-first: e.g. A+C, A+D, B+C, B+D for outer alternatives A/B and
  inner C/D.
- Additionally, for each alternative that involves a constrained
  `@Matches`/`@NotMatches` parameter, include a non-compliant (non-matching)
  example immediately followed by a compliant (matching) example. For
  alternatives without constrained parameters, include only a single
  (compliant) test case. If an alternative involves multiple constrained
  parameters, include one non-compliant example per constraint (each violating
  only that single constraint), followed by one fully-compliant example.
- For rules with `@NotMatches(IsRefasterAsVarargs.class)`, include a
  non-compliant example using `Refaster.asVarargs(...)` in the test input.
- Beyond the requirements listed above, do not unnecessarily include additional
  variants.
- In case more than one expression matches, return an `ImmutableSet<SomeType>`,
  with the expressions nested inside `return ImmutableSet.of(...)`. (Unless the
  matched expressions are of type `void`, of course.)
- Keep test expressions as simple as possible. Try not to repeat dummy
  sub-expressions unless required. For integers, use 1, 2, 3, etc. For strings
  use "foo", "bar", "baz", "qux", "quux", "corge", "grault", "garply", "waldo",
  "fred", "plugh", "xyzzy", "thud". For functional types use method references
  such as `Object::toString` or lambda expressions such as `() -> {}` and `()
  -> "foo"`. For `Throwable`, use `IllegalArgumentException` and
  `IllegalStateException`.
- Use of canonical dummy values is strictly required. Use them even for
  semantically-awkward cases (e.g. `assertThat(1).isNegative()`), since test
  expressions are not actually executed.
- When a `@BeforeTemplate` accepts a wide parameter type (e.g., `double`,
  `long`), tests should also cover common narrower-type variants that match via
  implicit widening (e.g., `BigDecimal.valueOf(0)` and
  `BigDecimal.valueOf(0L)`).
- For any type, use the simplest canonical expression available. Examples:
  `ImmutableList.of(1)`, `ImmutableList.of()`, `Mono.empty()`, `Mono.just(1)`,
  `Flux.just("foo")`, `new String[0]`, `new Object()`, `reverseOrder()`,
  `naturalOrder()`. Prefer single-element or empty collections over
  multi-element ones. Prefer `naturalOrder()` or `reverseOrder()` over
  `Comparator.comparing(...)`.
- Use distinct expressions for each distinct template method parameter and for
  each test case (when a rule uses `Refaster.anyOf` or has multiple
  `@BeforeTemplate` methods). For example, use `ImmutableList.of(1)` for the
  first expression, `ImmutableList.of(2)` for the second, etc. That said,
  **variation takes precedence over simplicity**: e.g., if a test needs three
  distinct comparators, using `Comparator.comparing(String::length)` as the
  third is acceptable after exhausting simpler alternatives like
  `naturalOrder()` and `reverseOrder()`.
- If a test method requires more distinct values than the dummy value list
  provides, cycle through the list from the beginning again.
- Don't unnecessarily use fully qualified types: unless there is a type clash,
  always import types.

### `elidedTypesAndStaticImports()`

When the test uses types or static imports that appear in the input
but not in the output (because the rule rewrites them away), override
`elidedTypesAndStaticImports()` to include a reference to those identifiers:

```java
@Override
public ImmutableSet<Object> elidedTypesAndStaticImports() {
  return ImmutableSet.of(
      SomeType.class,
      someStaticImport(null),
      (Runnable) () -> someVoidStaticImport(0));
}
```

This tells the test framework that these imports are expected to disappear from
the output. Only list types/imports that are not used by any `after-template`
test expression in the output file.

## Step 4 - Create the test output file

Create `{Topic}RulesTestOutput.java` in the same directory. It has
the **identical structure** as the input file but with the _expected
output_ after the rules are applied:

```java
package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class {Topic}RulesTest implements RefasterRuleCollectionTestCase {
  SomeType test{RuleName}() {
    return value.preferredAlternative();
  }
}
```

Conventions:
- Same class name, same method signatures, same structure.
- Only the method _bodies_ differ (they contain the rewritten code).
- Imports may differ (added as the rules dictate; removals shouldn't happen due
  to `elidedTypesAndStaticImports()` usage).

## Step 5 - Register the collection

Add the new rule collection class to the `RULE_COLLECTIONS` set in
[`RefasterRulesTest.java`][refaster-rules-test]. Entries are listed in
**alphabetical order**:

```java
private static final ImmutableSet<Class<?>> RULE_COLLECTIONS =
    ImmutableSet.of(
        // ...
        BigDecimalRules.class,
        // --> insert {Topic}Rules.class here, alphabetically
        BugCheckerRules.class,
        // ...
    );
```

## Step 6 - Verify

Run the tests to confirm that the rules compile and produce the expected
output:

```sh
mvn clean test -pl error-prone-contrib -Dtest=RefasterRulesTest -Dverification.skip
```

## Step 7 - Clean the codebase

Install the changes, apply the new rule(s) to the current repository, and
validate that as a result the whole build passes:

```sh
mvn clean install -DskipTests -Dverification.skip
./apply-error-prone-suggestions.sh
./run-full-build.sh
```

NB: Note that Refaster rules are not mutated by Pitest. As such, contrary to
any other code change in this project, changes that only introduce or modify
Refaster rules and associated tests do *not* require follow-up by running
`./run-branch-mutation-tests.sh`.

## Reference: Custom annotations (from `refaster-support`)

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@OnlineDocumentation` | Outer collection class | Links to generated documentation on the website |
| `@Description("...")` | Inner rule class | Overrides the default rule description |
| `@Severity(SeverityLevel.X)` | Inner rule class | Overrides the default severity level |
| `@TypeMigration(of=X.class, unmigratedMethods={"..."})` | Outer collection class | Marks a rule collection as part of a type migration. Lists method signatures that cannot yet be migrated, with comments explaining why (e.g., no target equivalent, complex semantics). See `JUnitToAssertJRules` and `TestNGToAssertJRules` for examples |

## Reference: Available matchers (for `@Matches` / `@NotMatches`)

| Matcher | What it checks |
|---------|---------------|
| `IsArray` | Expression type is an array |
| `IsCharacter` | Expression type is `char` or `Character` |
| `IsEmpty` | Expression is an empty collection, array, string literal or other type of expression considered "empty" |
| `IsIdentityOperation` | Expression is an identity function (e.g., `x -> x`, `identity()`) |
| `IsLambdaExpressionOrMethodReference` | Expression is a lambda or method reference |
| `IsList` | Expression type implements `List` |
| `IsMultidimensionalArray` | Expression type is a multidimensional array |
| `IsRefasterAsVarargs` | Expression is a `Refaster.asVarargs(...)` call |
| `RequiresComputation` | Expression is not a simple literal, identifier, or member select |
| `ReturnsMono` | Expression returns `Mono<T>` |
| `ThrowsCheckedException` | Expression may throw a checked exception |

## Common mistakes

1. **Forgetting to register** the collection in `RefasterRulesTest.java`
   `RULE_COLLECTIONS`.
2. **Wrong test method name**: must be `testFooBar()` matching the inner class
   name `FooBar` exactly.
3. **Missing `elidedTypesAndStaticImports()`**: if the input uses types/imports
   that the output does not, the test will fail unless these are listed in
   `elidedTypesAndStaticImports()` in both the input and output test file.
4. **Wrong test class name**: the class inside the test resource files must be
   named `{Topic}RulesTest`, not `{Topic}RulesTestInput` or
   `{Topic}RulesTestOutput`.
5. **Non-alphabetical registration**: entries in `RULE_COLLECTIONS` must be in
   alphabetical order.
6. **Using `abstract static class` without `@Placeholder`**: only use
   `abstract` when the rule needs `@Placeholder` methods; otherwise use `static
   final class`.
7. **Wildcard bounds in template method parameters**: using `? extends X` or `?
   super X` in `@BeforeTemplate`/`@AfterTemplate` method parameters instead of
   introducing additional class-level type parameters (see *Type parameter
   usage*).
8. **Before-templates that use pre-rewrite expressions**: using expressions in
   `@BeforeTemplate` methods that would already be rewritten by another rule;
   see *Principle 1* under "How rules interact with each other" above for
   examples and a checklist.
9. **Not following all test method conventions**: there's a long list of
   guidelines for creating or modifying the test input file, and it's very easy
   to overlook a requirement. Go over the list one-by-one, carefully, and
   update the test code if necessary.

[contributing]: ../../CONTRIBUTING.md
[refaster]: https://errorprone.info/docs/refaster
[refaster-rules-test]: ../../../error-prone-contrib/src/test/java/tech/picnic/errorprone/refasterrules/RefasterRulesTest.java
