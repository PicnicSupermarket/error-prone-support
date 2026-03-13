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
- Each rule is a `static final class` nested inside the outer class.
- Every rule has a Javadoc comment of the form `/** Prefer X over Y. */`, where
  `X` is generally a Javadoc link ({@link ...}) to the preferred alternative,
  and Y is a short qualitative description of the code in `@BeforeTemplate`s.
  If applicable, prefer one of the following variants (or combinations
  thereof):
  - "Prefer X over more verbose alternatives."
  - "Prefer X over more contrived alternatives."
  - "Prefer X over less idiomatic alternatives."
  - "Prefer X over less efficient alternatives."
  - "Prefer X over deprecated alternatives."
  - "Prefer X over non-JDK alternatives."
  - "Prefer X over less explicit alternatives."
  - "Prefer X over the associated constructor."
- `@BeforeTemplate` methods are named `before`. If there are multiple overloads
  with identical parameter types, then `before2`, `before3`, etc. are also
  used.
- The `@AfterTemplate` method is named `after`.
- Method parameters are listed in the order in which they first occur in the
  `@AfterTemplate` method.

### Rules as a term rewriting system

The full set of all defined rules is meant to be repeatedly applied to a code
base, until no further changes are required. In practice, this means that rules
must not conflict or be redundant. That is, a new rule should not contain a
`@BeforeTemplate` method with a (sub)expression that is rewritten by another
rule. Said rewrite must be assumed to have happened already, and so the
`@BeforeTemplate` method must use the replacement expression instead.

Conversely, introducing a new rule may also allow other rules to be simplified
or to become fully redundant. In this case, update or drop other rules as
applicable.

### Naming conventions

Rules are named after the code in their `@AfterTemplate` method,
following these guidelines:
- The name is derived by concatenating all non-variable non-`@PlaceHolder`
  identifiers as CamelCase.
- In case of instance method or field references, the type of the dereferenced
  parameter is also included.
- In case of name clashes, rename new or existing rules as necessary to
  disambiguate them:
  - Variants with more template methods parameters are amended to include
    `WithX` or `WithXAndY`, where `X and `Y` are the additional parameter
    type(s).
  - In case there's an expression (non-`void` returning) and block (`void`
    returning) variant, append `Expression` and `Block` as appropriate).
- When invoking static methods or fields, check the rest of the code base to
  assess whether a method or field is usually statically imported; if so,
  follow that style.

Examples:

| Code | Name |
|------------|------|
| `return BigDecimal.ZERO` | `BigDecimalZero;` |
| `return ImmutableList.sortedCopyOf(collection).iterator();` | `ImmutableListSortedCopyOfIterator` |
| `return ImmutableList.sortedCopyOf(cmp, collection).iterator();` | `ImmutableListSortedCopyOfIterator` or `ImmutableListSortedCopyOfIteratorWithComparator` |
| `return addTo.addAll(elementsToAdd);` | `CollectionAddAll` or `CollectionAddAllExpression` |
| `addTo.addAll(elementsToAdd);` | `CollectionAddAll` or or `CollectionAddAllBlock` |
| `return stream.collect(toImmutableMap(e -> keyFunction(e), e -> valueFunction(e))); | `StreamCollectToImmutableMap` |

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

### Type parameter usage

Template method parameters should avoid wildcard bounds where possible. Thus,
do *not* write this:

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

## Step 2 - Advanced patterns

### `Refaster.anyOf(...)` for multiple before-patterns

Match several equivalent patterns in a single `@BeforeTemplate`:

```java
@BeforeTemplate
BigDecimal before() {
  return Refaster.anyOf(BigDecimal.valueOf(0), new BigDecimal("0"));
}
```

### `@AlsoNegation` for auto-negated variants

When applied to the `@AfterTemplate`, Refaster automatically
generates a negated variant (e.g., `== 0` also matches `!= 0`):

```java
@AfterTemplate
@AlsoNegation
boolean after(BigDecimal value) {
  return value.signum() == 0;
}
```

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
class must be `abstract static` (not `static final`):

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
    return Collections.min(Arrays.asList(value), cmp);
  }
}
```

### `@UseImportPolicy(STATIC_IMPORT_ALWAYS)`

Force the replacement to use a static import:

```java
@AfterTemplate
@UseImportPolicy(STATIC_IMPORT_ALWAYS)
Comparator<T> after() {
  return naturalOrder();
}
```

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
- The test method body contains the _before_ code (the code that should be
  matched and rewritten).
- Test method order must match rule declaration order.
- When a rule uses `@AlsoNegation`, include both the positive and negated cases
  in the test method.
- Make sure that all variants of before-templates are matched, considering also
  all `Refaster.anyOf` variants.
- In case of `@Matches`/`@NotMatches` constraints, include compliant and
  non-compliant variants.
- Beyond the requirements listed above, do not unnecessarily include additional
  variants.
- In case more than one expression matches, return an `ImmutableSet<SomeType>`,
  with the expressions nested inside `return ImmutableSet.of(...)`. (Unless the
  matched expressions are of type `void`, of course.)
- Keep test expressions as simple as possible. Try not tyo repeat dummy
  sub-expressions unless required. For integers, use 1, 2, 3, etc. For strings
  use "foo", "bar", "baz", "qux", "quux", "corge", "grault", "garply", "waldo",
  "fred", "plugh", "xyzzy", "thud".
- Don't unnecessarily use fully qualified types.

### `elidedTypesAndStaticImports()`

When the test uses types or static imports that appear in the input
but not in the output (because the rule rewrites them away), override
`elidedTypesAndStaticImports()`:

```java
@Override
public ImmutableSet<Object> elidedTypesAndStaticImports() {
  return ImmutableSet.of(
      Arrays.class,
      Collections.class,
      identity());
}
```

This tells the test framework that these imports are expected to disappear from
the output.

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
mvn test -pl error-prone-contrib -Dtest=RefasterRulesTest -Dverification.skip
```

Note that Refaster rules are not mutated by Pitest. As such, contrary to any
other code change in this project, changes that only introduce or modify
Refaster rules and associated tests do *not* require follow-up by running
`./run-branch-mutation-tests.sh`.

## Reference: Custom annotations (from `refaster-support`)

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@OnlineDocumentation` | Outer class | Links to generated documentation on the website |
| `@Description("...")` | Inner rule class | Overrides the default rule description |
| `@Severity(SeverityLevel.X)` | Inner rule class | Overrides the default severity level |
| `@TypeMigration(of=X.class, unmigratedMethods={"..."})` | Inner rule class | Marks a rule as part of a type migration |

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
8. **Before-templates that are overly specific**: using expressions in
   `@BeforeTemplate` methods that contain a subexpression matched by another
   template; in this case the replacement expression must be used instead.

[contributing]: ../../CONTRIBUTING.md
[refaster]: https://errorprone.info/docs/refaster
[refaster-rules-test]: ../../../error-prone-contrib/src/test/java/tech/picnic/errorprone/refasterrules/RefasterRulesTest.java
