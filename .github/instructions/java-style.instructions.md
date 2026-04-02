---
applyTo: "**/*.java"
---

# Java Style Conventions

This document serves as the canonical reference for Java code style conventions
in this repository, for both AI coding agents and human contributors. It covers
conventions that are specific to this project and not common Java idioms that
developers or agents would follow by default.

The project's self-check (`./apply-error-prone-suggestions.sh`) will auto-fix
many style violations, but writing clean code from the start avoids unnecessary
churn. Follow these conventions so that first drafts require minimal cleanup.

## Collections and immutability

### Use Guava immutable collections

Use `ImmutableList`, `ImmutableSet`, `ImmutableMap`, `ImmutableListMultimap`,
`ImmutableSetMultimap`, `ImmutableBiMap`, `ImmutableSortedSet`,
`ImmutableSortedMap`, `ImmutableRangeMap`, `ImmutableRangeSet`,
`ImmutableTable`, and other Guava immutable types instead of their mutable
counterparts. This is the single most important style rule in this codebase.

**Do:**

```java
private static final ImmutableSet<String> NAMES =
    ImmutableSet.of("foo", "bar");

ImmutableList<String> result = items.stream()
    .filter(Item::isActive)
    .map(Item::name)
    .collect(toImmutableList());
```

**Don't:**

```java
private static final Set<String> NAMES = Set.of("foo", "bar");

List<String> result = items.stream()
    .filter(Item::isActive)
    .map(Item::name)
    .toList();
```

### Use Guava immutable collectors

Use `toImmutableList()`, `toImmutableSet()`, `toImmutableMap()` and other Guava
stream collectors. Statically import them.

**Do:**

```java
import static com.google.common.collect.ImmutableList.toImmutableList;

ImmutableList<String> names =
    items.stream().map(Item::name).collect(toImmutableList());
```

**Don't:**

```java
List<String> names =
    items.stream().map(Item::name).collect(Collectors.toList());
List<String> names = items.stream().map(Item::name).toList();
```

### Use immutable types for parameters and return types

Method return types should be `ImmutableList`, `ImmutableSet`,
`ImmutableMap` or other (Guava) immutable types. Method parameters should also
prefer immutable types where practical, as this makes it clear that the method
does not mutate its input. Use broader interfaces (`Iterable`, `Collection`)
only when the method genuinely needs to accept mutable collections from callers
outside this project.

**Do:**

```java
public static ImmutableList<MethodTree> findMethods(
    String name, ImmutableList<Tree> members) {
  return members.stream()
      .filter(MethodTree.class::isInstance)
      .map(MethodTree.class::cast)
      .filter(m -> m.getName().contentEquals(name))
      .collect(toImmutableList());
}
```

**Don't:**

```java
public static List<MethodTree> findMethods(
    String name, List<Tree> members) { ... }
```

### Use builder pattern for conditional collection construction

When building collections incrementally or conditionally, use `.builder()` and
`.build()`.

**Do:**

```java
ImmutableList.Builder<ReturnTree> returns = ImmutableList.builder();
// ... conditionally add elements ...
return returns.build();
```

### Use Guava `Multimap` instead of `Map<K, Collection<V>>`

Use `ImmutableListMultimap` or `ImmutableSetMultimap` instead of e.g. `Map<K,
List<V>>`, `Map<K, Set<V>>`, or `ImmutableMap<K, ? extends Collection<V>>`. A
`Map` with collection values is almost never the desired data type; use a
`Multimap` instead.

Use Guava's `toImmutableListMultimap()` / `toImmutableSetMultimap()` collectors
instead of `Collectors.groupingBy()`. When the value mapping itself produces a
collection that should be flattened, use `flatteningToImmutableListMultimap()`
or `flatteningToImmutableSetMultimap()`.

**Do:**

```java
ImmutableListMultimap<String, Item> byCategory =
    items.stream()
        .collect(
            toImmutableListMultimap(
                Item::category, identity()));

ImmutableSetMultimap<String, String> tagsByCategory =
    items.stream()
        .collect(
            flatteningToImmutableSetMultimap(
                Item::category, item -> item.tags().stream()));
```

**Don't:**

```java
Map<String, List<Item>> byCategory = items.stream()
    .collect(Collectors.groupingBy(Item::category));

ImmutableMap<String, ImmutableList<String>> tagsByCategory = ...;
```

## Nullability and optionals

### Prefer `Optional` over `@Nullable`

Use `Optional` for return types and parameters instead of `@Nullable`. This is
more explicit about the possibility of absence and avoids null checks. While
using `Optional` as a parameter type is uncommon in the broader Java ecosystem,
this project prefers it over `@Nullable` parameters because it makes the
optionality visible in the type system.

**Do:**

```java
public static Optional<MethodTree> findMethodExitedOnReturn(
    VisitorState state) { ... }
public static void process(Optional<String> name) { ... }
```

**Don't:**

```java
public static @Nullable MethodTree findMethodExitedOnReturn(
    VisitorState state) { ... }
public static void process(@Nullable String name) { ... }
```

### Use JSpecify `@Nullable` and `@NullMarked`

Use `@Nullable` from `org.jspecify.annotations` exclusively. Never use
`@Nullable` from other packages (javax.annotation, etc.). Apply `@NullMarked`
at the package level in every `package-info.java`.

**Do:**

```java
// package-info.java
@CheckReturnValue
@NullMarked
package tech.picnic.errorprone.bugpatterns;

// Source file
import org.jspecify.annotations.Nullable;

public @Nullable String getName() { ... }
```

### Use `requireNonNull` with a descriptive message

Always statically import `requireNonNull` from `java.util.Objects`. When used
for validation, always provide a message.

**Do:**

```java
import static java.util.Objects.requireNonNull;

ModifiersTree modifiers =
    requireNonNull(
        ASTHelpers.getModifiers(tree), "Tree must have modifiers");
```

**Don't:**

```java
// No message, not statically imported.
Objects.requireNonNull(ASTHelpers.getModifiers(tree));
```

### Use `requireNonNullElse` instead of Guava's `firstNonNull`

Use `Objects.requireNonNullElse()` or `Objects.requireNonNullElseGet()` instead
of `MoreObjects.firstNonNull()` or `Optional.ofNullable(...).orElse(...)`.

## Functional style

### Favour early returns, simplest cases first

Use early returns (guard clauses) to handle simple or non-matching cases at the
top of a method, reducing nesting. Address the simplest cases first, even if
that means testing for a negation.

**Do:**

```java
private static Optional<Fix> tryFix(Tree tree, VisitorState state) {
  if (!(tree instanceof MethodInvocationTree invocation)) {
    return Optional.empty();
  }
  if (!RELEVANT_METHOD.matches(invocation, state)) {
    return Optional.empty();
  }
  return Optional.of(buildFix(invocation, state));
}
```

**Don't:**

```java
private static Optional<Fix> tryFix(Tree tree, VisitorState state) {
  if (tree instanceof MethodInvocationTree invocation) {
    if (RELEVANT_METHOD.matches(invocation, state)) {
      return Optional.of(buildFix(invocation, state));
    }
  }
  return Optional.empty();
}
```

### Prefer ternary return over `if`/`return` blocks

When a method returns one of two values based on a condition and readability is
not impacted, prefer a ternary expression over an `if`/`return` block.

**Do:**

```java
return IS_CONSTANT.matches(tree, state)
    ? describeMatch(tree)
    : Description.NO_MATCH;
```

**Don't:**

```java
if (IS_CONSTANT.matches(tree, state)) {
  return describeMatch(tree);
}
return Description.NO_MATCH;
```

### Prefer `Consumer<T>` sink parameters over mutable collection parameters

When a method accepts an `ImmutableList.Builder<T>`, a mutable `List<T>`, or a
similar mutable collection parameter and only calls a single mutation method on
it (e.g., `builder.add(element)` or `list.add(element)`), express the parameter
as `Consumer<T>` instead. Callers pass `builder::add` or `list::add`, which
decouples the method from the specific collection type and makes the intent
clearer. This applies especially to visitor and traversal methods with
side-effects.

**Do:**

```java
private static void collectReturns(Tree tree, Consumer<ReturnTree> sink) {
  new TreeScanner<@Nullable Void, @Nullable Void>() {
    @Override
    public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
      sink.accept(node);
      return null;
    }
  }.scan(tree, null);
}

// Caller:
ImmutableList.Builder<ReturnTree> returns = ImmutableList.builder();
collectReturns(tree, returns::add);
```

**Don't:**

```java
private static void collectReturns(
    Tree tree, ImmutableList.Builder<ReturnTree> builder) {
  // ... builder.add(node) ...
}

private static void collectReturns(
    Tree tree, List<ReturnTree> results) {
  // ... results.add(node) ...
}
```

### Prefer streams over imperative loops

Use `Stream` pipelines instead of `for` loops where the result is a
transformation, filter, or aggregation. This codebase has a roughly 10:1
stream-to-loop ratio.

**Do:**

```java
ImmutableList<String> names = members.stream()
    .filter(MethodTree.class::isInstance)
    .map(MethodTree.class::cast)
    .filter(m -> m.getName().contentEquals(methodName))
    .collect(toImmutableList());
```

**Don't:**

```java
List<String> names = new ArrayList<>();
for (Tree member : members) {
  if (member instanceof MethodTree method
      && method.getName().contentEquals(methodName)) {
    names.add(method);
  }
}
```

### Prefer method references over lambdas

Use method references (`Foo::bar`) when possible. Use lambdas only when the
logic requires more than a single method call. For negated predicates, prefer
`not(Foo::bar)` with a static import of `java.util.function.Predicate.not` over
writing a lambda.

**Do:**

```java
import static java.util.function.Predicate.not;

.filter(MethodTree.class::isInstance)
.map(MethodTree.class::cast)
.filter(not(SomeUtilMethod::isDefault))
```

**Don't:**

```java
.filter(t -> t instanceof MethodTree)
.map(t -> (MethodTree) t)
.filter(m -> !SomeUtilMethod.isDefault(m))
```

### Do not pass `Stream` as a method parameter

Avoid accepting `Stream` as a parameter type. Materialize the stream into a
collection first, then pass the collection. This avoids issues with stream
reuse and makes the API more predictable.

### Inline single-use fields and variables

If a field, constant, or local variable is used only once, inline it unless
extracting it meaningfully improves readability or performance.

## Error Prone API usage

These conventions apply primarily to `BugChecker` implementation code, but also
to logic in the `error-prone-utils` and `refaster-support` modules.

### Define matchers as `private static final` fields

All `Matcher<T>` instances must be declared as `private static final` fields
with `UPPER_SNAKE_CASE` names. Use `IS_*` or descriptive names.

**Do:**

```java
private static final Matcher<ExpressionTree> OPTIONAL_OR_ELSE =
    instanceMethod()
        .onExactClass(Optional.class.getCanonicalName())
        .namedAnyOf("orElse");
private static final Matcher<VariableTree> IS_CONSTANT =
    allOf(hasModifier(Modifier.STATIC), hasModifier(Modifier.FINAL));
```

**Don't:**

```java
// Don't create matchers inline in match methods.
@Override
public Description matchMethodInvocation(
    MethodInvocationTree tree, VisitorState state) {
  if (instanceMethod()
      .onExactClass("java.util.Optional")
      .named("orElse")
      .matches(tree, state)) {
```

### Prefer static `Matcher` fields over manual boolean predicate methods

Where possible, express matching logic as a composed static `Matcher` field
rather than a `boolean somePredicate(Tree, VisitorState)` helper method. Static
matchers are composable with `allOf()`, `anyOf()`, and `not()`, and can be used
directly in other matchers. Use helper methods only when the logic is too
complex for matcher composition (e.g., requires local variables or recursive
traversal).

**Do:**

```java
private static final Matcher<ExpressionTree>
    IS_STATIC_ENCLOSING_CLASS =
        classLiteral(MyChecker::isEnclosingClassReference);
private static final Matcher<ExpressionTree>
    IS_DYNAMIC_ENCLOSING_CLASS =
        toType(
            MethodInvocationTree.class,
            allOf(
                instanceMethod()
                    .anyClass()
                    .named("getClass")
                    .withNoParameters(),
                MyChecker::getClassReceiverIsEnclosingClassInstance));
```

**Don't:**

```java
private static boolean isRelevant(
    ExpressionTree tree, VisitorState state) {
  return staticMethod()
      .onClass("com.example.Foo")
      .named("bar")
      .matches(tree, state);
}
```

### Prefer `instanceof` pattern matching over `Tree#getKind()`

Use `instanceof` with pattern matching for tree type checks. Use `getKind()`
only when checking tree kind enums that do not correspond to distinct tree
types (e.g., `Kind.PLUS`).

**Do:**

```java
if (!(tree instanceof MethodInvocationTree methodInvocation)) {
  return Optional.empty();
}
if (!(methodInvocation.getMethodSelect()
    instanceof MemberSelectTree memberSelect)) {
  return Optional.empty();
}
```

**Don't:**

```java
if (tree.getKind() != Tree.Kind.METHOD_INVOCATION) {
  return Optional.empty();
}
MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;
```

### Compose matchers with `allOf()`, `anyOf()`, `not()`

Statically import matcher builder methods from
`com.google.errorprone.matchers.Matchers` and compose them functionally.

**Do:**

```java
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.not;

private static final Matcher<ExpressionTree> MOCKITO_MOCK =
    allOf(
        not(
            toType(
                MethodInvocationTree.class,
                argument(0, isSameType(Class.class.getCanonicalName())))),
        staticMethod()
            .onClass("org.mockito.Mockito")
            .namedAnyOf("mock", "spy"));
```

### Use `ASTHelpers` qualified, not statically imported

Always call `ASTHelpers` methods with the class qualifier. This is a universal
convention in the codebase.

**Do:**

```java
MethodSymbol method = ASTHelpers.getSymbol(tree);
Type type = ASTHelpers.getType(expression);
ExpressionTree receiver = ASTHelpers.getReceiver(invocation);
```

**Don't:**

```java
import static com.google.errorprone.util.ASTHelpers.getSymbol;

MethodSymbol method = getSymbol(tree);
```

### Use `error-prone-utils` utilities
<!-- check: Use `SourceCode`, `MoreASTHelpers`, `Documentation` from `error-prone-utils` -->

Reuse shared utilities from the `error-prone-utils` module instead of
reimplementing common operations:

- `MoreASTHelpers`: find methods, return statements in enclosing class.
- `SourceCode.treeToString()`: prefer original source representation.
- `MoreMatchers`: additional matcher builders.
- `MoreTypePredicates`: type predicate builders with memoization.
- `ConflictDetection.findMethodRenameBlocker()`: validate renames.
- `ThirdPartyLibrary`: guard library-dependent fixes.
- `Documentation.BUG_PATTERNS_BASE_URL`: documentation link base URL.
- `Flags.getList()` / `Flags.getSet()`: parse Error Prone flag values.

### Use guard clauses for `Description.NO_MATCH`

Structure match methods with early returns for `NO_MATCH`. For complex logic,
use `Optional` chains that terminate with `.orElse(NO_MATCH)`.

**Do:**

```java
@Override
public Description matchMethodInvocation(
    MethodInvocationTree tree, VisitorState state) {
  if (!OPTIONAL_OR_ELSE.matches(tree, state)) {
    return Description.NO_MATCH;
  }
  return tryConvertToMethodReference(tree, state)
      .map(ref -> describeMatch(tree, SuggestedFix.replace(argument, ref)))
      .orElse(Description.NO_MATCH);
}
```

### Avoid `compilesWithFix` in `BugChecker` implementations

Do not use `SuggestedFixes.compilesWithFix` to validate fixes. It recompiles
the entire file after applying the fix, which is extremely expensive and can
prevent checks from being enabled in large codebases.

### `VisitorState` comes last in method signatures

When a method accepts a `VisitorState` parameter, it must always be the last
parameter. This rule may be deviated from only if Error Prone's
`InconsistentOverloads` rule requires it.

### `@BugPattern` summary should not end with a period

The `summary` attribute of `@BugPattern` is the user-facing error message. Keep
it concise and do not end it with a period.

### `BugChecker` class Javadoc format
<!-- check: BugChecker Javadoc: first line is a `{@link}` to the replaced/flagged element -->

Start `BugChecker` class Javadoc with "A {@link BugChecker} that...".

**Do:**

```java
/**
 * A {@link BugChecker} that flags AssertJ equality checks on unwrapped
 * {@link Optional} instances.
 */
```

## Imports and formatting

### No wildcard imports

Never use wildcard imports. Always import specific classes.

### Google Java Format
<!-- check: Code formatted with `mvn fmt:format` -->

Formatting is enforced using Google Java Format. Run `mvn fmt:format` before
committing. Never hand-format code.

## Javadoc and comments

### Use `{@code}` and `{@link}` in Javadoc

Use `{@code ...}` for inline code snippets and identifiers. Use `{@link ...}`
for cross-references to types and methods. Never use backticks or `<code>` tags
in Javadoc.

**Do:**

```java
/**
 * Returns the {@link MethodTree}s matching the given {@code methodName} in the
 * enclosing class.
 *
 * @param methodName The method name to search for.
 * @param state The {@link VisitorState} from which to derive the enclosing class.
 * @return The matching {@link MethodTree}s.
 */
```

### Use `<p>` for paragraph breaks

Start new paragraphs in Javadoc with a `<p>` tag on a new line. Do not use
closing `</p>` tags.

**Do:**

```java
/**
 * First paragraph describing the main purpose.
 *
 * <p>Second paragraph with additional details. This paragraph explains the
 * edge cases and any caveats to be aware of.
 */
```

### Apply `@SuppressWarnings` to the smallest possible scope

Every `@SuppressWarnings` entry must have a `/* ... */` comment explaining why
the suppression is necessary. Apply suppressions to the narrowest scope
possible: prefer annotating a local variable declaration over an entire method,
and a method over an entire class. Outside of Refaster rules, consider
factoring out a variable so that the suppression applies only to its
initialization expression. Class-level suppressions should be used only when a
smaller-scope suppression is impossible or impractical.

If multiple suppression entries are required on a single annotation, add an
additional
`"z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"`
entry at the end of the list. This self-documenting entry is the only entry
that must *not* come with a comment.

### Use `// XXX:` for future work, not `// TODO:`

Mark known limitations, future improvements, and technical debt with `// XXX:`
comments (with colon). Never use `// TODO:` or `// FIXME:`.

**Do:**

```java
// XXX: Also match effectively final variables that reference provably-empty
// objects.
// XXX: Consider supporting custom matchers for additional container types.
```

### Javadoc sentences must end with a period

Every Javadoc sentence (including the summary line, `@param` descriptions, and
`@return` descriptions) must end with a period.

### Always include `@param`, `@return`, and `@throws` tags

Include these tags on all public methods, even if obvious. (Checkstyle enforces
their presence on public types and type members.)

### Private methods generally do not need Javadoc

If a private method needs documentation, a single summary line suffices. Do not
add `@param`/`@return` tags to private methods.

## Class structure and naming

### Prefer `final` classes and minimal visibility

All classes should be `final` unless there is a compelling reason for
subclassing (e.g., an abstract base class). Similarly, all types and members
should have the least visibility possible: prefer `private` over
package-private, package-private over `protected`, and `protected` over
`public`.

**Do:**

```java
public final class MyChecker extends BugChecker implements MethodTreeMatcher {
  private static final Matcher<ExpressionTree> RELEVANT =
      staticMethod().onClass("com.example.Foo");
  private final ImmutableSet<String> names;
}
```

**Don't:**

```java
public class MyChecker extends BugChecker implements MethodTreeMatcher {
  protected static final Matcher<ExpressionTree> RELEVANT =
      staticMethod().onClass("com.example.Foo");
  ImmutableSet<String> names;
}
```

### Class member ordering
<!-- check: Fields are ordered: static final, static non-final, instance final, instance non-final -->
<!-- check: Methods are ordered: constructors, static factory methods, overrides, then by usage order: first instance, then static -->

Within a class, order members as follows:

**Fields** (in this order):

1. `private static final long serialVersionUID = 1L;` (if applicable).
2. `private static final` matcher fields.
3. `private static final` other constants (strings, patterns, sets).
4. `static` non-final fields (rare; avoid when possible).
5. Instance `final` fields.
6. Instance non-final fields (rare; avoid when possible).

Within each group, list fields that are immediately initialized (at
declaration) before those initialized by a constructor. Sort fields
lexicographically within the same initialization category.

**Methods** (in this order):

1. Constructors.
2. Static factory methods.
3. Public methods (`@Override` methods in the order dictated by the interface
   enumeration, and their declarations within those interfaces).
4. Private instance helper methods.
5. Private static helper methods.

Methods should "call down": a method should be declared above the methods it
calls.

**Inner classes** go at the bottom of the enclosing class, even if they are
referenced earlier.

### No empty first line inside a class body

Do not add a blank line between the class declaration and the first member. The
first field or method should immediately follow the opening brace.

### Keep lists and members sorted lexicographically

Sort lists of identifiers, enum constants, map entries, annotation attributes,
and similar enumerations lexicographically. This includes entries in
`ImmutableSet.of(...)`, `ImmutableMap.of(...)`, and similar constructs.

More generally, any enumeration that can be reordered without impacting
semantics should be ordered lexicographically.

### Utility class pattern
<!-- check: Utility classes are `final` with a private constructor -->

Utility classes must be `final` with a private no-arg constructor.

**Do:**

```java
public final class MoreASTHelpers {
  private MoreASTHelpers() {}

  public static ImmutableList<MethodTree> findMethods(...) { ... }
}
```

### Naming conventions
<!-- check: Class and method names follow project naming conventions -->

- Constants: `UPPER_SNAKE_CASE` (exception: `serialVersionUID`).
- Matcher fields: `IS_*`, `HAS_*`, or descriptive `UPPER_SNAKE_CASE`. Use
  `_METHOD` suffix for method matchers.
- Matcher classes: `Is*`, `Returns*`, `Throws*`, `Requires*`.
- Utility classes: `More*` (when extending framework classes).
- Reserve the `try` prefix for `Optional`-returning methods. Use `find` for
  search methods that return `Optional`.
- Do not include the collection type in variable names: use `names` instead of
  `namesList`, `entries` instead of `entryMap`.

### `BugChecker` constructor pattern
<!-- check: `BugChecker` has a public no-arg constructor with Javadoc -->

`BugChecker`s that support configuration flags use a two-constructor pattern: a
public no-arg constructor that delegates to a package-private `@Inject`
constructor.

```java
public MyChecker() {
  this(ErrorProneFlags.empty());
}

@Inject
MyChecker(ErrorProneFlags flags) {
  this.exemptedNames =
      Sets.union(
              DEFAULTS,
              Flags.getSet(flags, FLAG_NAME))
          .immutableCopy();
}
```

## Java language features

### Use text blocks for multi-line strings

Use text blocks (`"""`) for multi-line string literals, especially in
`@BugPattern` summaries and test source code.

### Use `instanceof` pattern matching

Prefer `instanceof` with a binding variable over a separate `instanceof` check
and cast. This also applies to negated patterns.

**Do:**

```java
if (tree instanceof MethodInvocationTree invocation) {
  ...
}
if (!(tree instanceof TryTree tryTree)) {
  return;
}
```

### Use switch expressions

Prefer switch expressions with arrow syntax (`->`) over traditional switch
statements. Combine with pattern matching where applicable.

**Do:**

```java
return switch (tree) {
  case LiteralTree literal -> false;
  case ArrayAccessTree access -> matches(access.getExpression());
  default -> ASTHelpers.constValue(tree) == null;
};
```

### Use records for immutable data carriers

Use `record` types for simple immutable value objects.

### Do not use `var`

This codebase does not use `var` (local variable type inference). Always use
explicit types.

**Do:**

```java
ImmutableList<String> names =
    items.stream().map(Item::name).collect(toImmutableList());
```

**Don't:**

```java
var names = items.stream().map(Item::name).collect(toImmutableList());
```

## Guava utilities

### Use `Preconditions` for validation

Statically import and use `checkArgument()`, `checkState()`, and
`checkNotNull()` for precondition checks.

```java
import static com.google.common.base.Preconditions.checkArgument;

checkArgument(clazz != null, "Visited node is not enclosed by a class");
```

### Use Guava collection utilities for set algebra

Use `Sets.union()`, `Sets.intersection()`, and `Sets.difference()` for set
operations. Unless the result is used only once in a local scope, call
`.immutableCopy()` on the result.

**Do:**

```java
ImmutableSet<String> combined =
    Sets.union(DEFAULT_NAMES, Flags.getSet(flags, EXTRA_NAMES_FLAG))
        .immutableCopy();
```
