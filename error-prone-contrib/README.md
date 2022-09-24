# Picnic's Error Prone Contrib

This project provides a plugin containing a collection of [Error
Prone][error-prone] checks.

## How to contribute

Contributions are more than welcome! Below we list tasks that are on our TODO
list. If you have others ideas/plans, feel free to file an issue or open a pull
request.

### Building

See the main [readme][main-readme].

### Contribution guidelines

See our [contributing guidelines][main-contributing].

### Our wishlist

We expect the following tasks to help improve the quality of this open source
project:

- Document how to apply patches.
- Document each of the checks.
- Add [SonarQube][sonarcloud] and [Codecov][codecov] integrations.
- Add non-Java file formatting support, like we have internally at Picnic.
  (I.e., somehow open-source that stuff.)
- Auto-generate a website listing each of the checks, just like the Error Prone
  [bug patterns page][error-prone-bug-patterns]. The [Error Prone
  repository][error-prone-repo] contains code for this.
- Set up a script/procedure for testing the plugin against other open source
  projects. By running the checks against a variety of code bases we are more
  likely to catch errors due to unexpected code constructs. False positives and
  regressions can be caught this way as well. For inspiration, review how
  [Checkstyle does this][checkstyle-external-project-tests].
- Have the repository be analyzed by [Better Code Hub][bettercodehub] and
  potentially publish the results.
- Consider integrating with [FOSSA][fossa].
- Review all places in the code where a `Description` is currently created, and
  see whether a custom error message (`Description.Builder#setMessage`) is
  warranted.
- Improve an existing check (see `XXX`-marked comments in the code) or write a
  new one (see the list of suggestions below).

### BugChecker extension ideas

The following is a list of checks we'd like to see implemented:

- A check with functionality equivalent to the "[Policeman's Forbidden API
  Checker][forbidden-apis]" Maven plugin, with a focus on disallowing usage of
  the methods and fields listed by the "JDK System Out" and "JDK Unsafe"
  signature groups. Using Error Prone's method matchers forbidden method calls
  can easily be identified. But Error Prone can go one step further by
  auto-patching violations. For each violation two fixes can be proposed: a
  purely behavior-preserving fix which makes the platform-dependent behavior
  explicit, and another which replaces the platform-dependent behavior with the
  preferred alternative. (Such as using `UTF-8` instead of the system default
  charset.)
- The [Modernizer Maven Plugin][modernizer-maven-plugin] can similarly be
  replaced with an equivalent Error Prone check. The latter would allow
  automatic replacement of outdated APIs through Error Prone's patch
  functionality.
- A subset of the refactor operations provided by the Eclipse-specific
  [AutoRefactor][autorefactor] plugin.
- A check that replaces fully qualified types with simple types in contexts
  where this does not introduce ambiguity. Should consider both actual Java
  code and Javadoc `@link` references.
- A check that simplifies array expressions. It would replace empty array
  expressions of the form `new int[] {}` with `new int[0]`. Statements of the
  form `byte[] arr = new byte[] {'c'};` would be shortened to `byte[] arr =
  {'c'};`.
- A check that replaces expressions of the form `String.format("some prefix
  %s", arg)` with `"some prefix " + arg`, and similar for simple suffixes. Can
  perhaps be generalized further, though it's unclear how far. (Well, a
  `String.format` call without arguments can certainly be simplified, too.)
- A check that replaces single-character strings with `char`s where possible.
  For example as argument to `StringBuilder.append` and in string
  concatenations.
- A check that adds or removes the first `Locale` argument to `String.format`
  and similar calls as necessary. (For example, a format string containing only
  `%s` placeholders is locale-insensitive unless any of the arguments is a
  `Formattable`, while `%f` placeholders _are_ locale-sensitive.)
- A check that replaces `String.replaceAll` with `String.replace` if the first
  argument is certainly not a regular expression. And if both arguments are
  single-character strings then the `(char, char)` overload can be invoked
  instead.
- A check that flags (and ideally, replaces) `try-finally` constructs with
  equivalent `try-with-resources` constructs.
- A check that drops exceptions declared in `throws` clauses if they are (a)
  not actually thrown and (b) the associated method cannot be overridden.
- A check that tries to statically import certain methods whenever used, if
  possible. The set of targeted methods should be configurable, but may default
  to e.g. `java.util.Function.identity()`, the static methods exposed by
  `java.util.stream.Collectors` and the various Guava collector factory
  methods.
- A check that replaces `new Random().someMethod()` calls with
  `ThreadLocalRandom.current().someMethod()` calls, to avoid unnecessary
  synchronization.
- A check that drops `this.` from `this.someMethod()` calls and which
  optionally does the same for fields, if no ambiguity arises.
- A check that replaces `Integer.valueOf` calls with `Integer.parseInt` or vice
  versa in order to prevent auto (un)boxing, and likewise for other number
  types.
- A check that flags nullable collections.
- A check that flags `AutoCloseable` resources not managed by a
  `try-with-resources` construct. Certain subtypes, such as jOOQ's `DSLContext`
  should be excluded.
- A check that flags `java.time` methods which implicitly consult the system
  clock, suggesting that a passed-in `Clock` is used instead.
- A check that flags public methods on public classes which reference
  non-public types. This can cause `IllegalAccessError`s and
  `BootstrapMethodError`s at runtime.
- A check that swaps the LHS and RHS in expressions of the form
  `nonConstant.equals(someNonNullConstant)`.
- A check that annotates methods which only throw an exception with
  `@Deprecated` or ` @DoNotCall`.
- A check that flags imports from other test classes.
- A Guava-specific check that replaces `Joiner.join` calls with `String.join`
  calls in those cases where the latter is a proper substitute for the former.
- A Guava-specific check that flags `{Immutable,}Multimap` type usages where
  `{Immutable,}{List,Set}Multimap` would be more appropriate.
- A Guava-specific check that rewrites `if (conditional) { throw new
  IllegalArgumentException(); }` and variants to an equivalent `checkArgument`
  statement. Idem for other exception types.
- A Guava-specific check that replaces simple anonymous `CacheLoader` subclass
  declarations with `CacheLoader.from(someLambda)`.
- A Spring-specific check that enforces that methods with the `@Scheduled`
  annotation are also annotated with New Relic's `@Trace` annotation. Such
  methods should ideally not also represent Spring MVC endpoints.
- A Spring-specific check that enforces that `@RequestMapping` annotations,
  when applied to a method, explicitly specify one or more target HTTP methods.
- A Spring-specific check that looks for classes in which all `@RequestMapping`
  annotations (and the various aliases) specify the same `path`/`value`
  property and then moves that path to the class level.
- A Spring-specific check that flags `@Value("some.property")` annotations, as
  these almost certainly should be `@Value("${some.property}")`.
- A Spring-specific check that drops the `required` attribute from
  `@RequestParam` annotations when the `defaultValue` attribute is also
  specified.
- A Spring-specific check that rewrites a class which uses field injection to
  one which uses constructor injection. This check wouldn't be strictly
  behavior preserving, but could be used for a one-off code base migration.
- A Spring-specific check that disallows field injection, except in
  `AbstractTestNGSpringContextTests` subclasses. (One known edge case:
  self-injections so that a bean can call itself through an implicit proxy.)
- A Spring-specific check that verifies that public methods on all classes
  whose name matches a certain pattern, e.g. `.*Service`, are annotated
  `@Secured`.
- A Spring-specific check that verifies that annotations such as
  `@RequestParam` are only present in `@RestController` classes.
- A Spring-specific check that disallows `@ResponseStatus` on MVC endpoint
  methods, as this prevents communication of error status codes.
- A Hibernate Validator-specific check that looks for `@UnwrapValidatedValue`
  usages and migrates the associated constraint annotations to the generic type
  argument to which they (are presumed to) apply.
- A TestNG-specific check that drops method-level `@Test` annotations if a
  matching/more specific annotation is already present at the class level.
- A TestNG-specific check that enforces that all tests are in a group.
- A TestNG-specific check that flags field assignments in
  `@BeforeMethod`-annotated methods unless the class is annotated
  `@Test(singleThreaded = true)`.
- A TestNG-specific check that flags usages of the `expectedExceptions`
  attribute of the `@Test` annotation, pointing to `assertThrows`.
- A Jongo-specific check that disallows the creation of sparse indices, in
  favour of partial indices.
- An Immutables-specific check that replaces
  `checkState`/`IllegalStateException` usages inside a `@Value.Check`-annotated
  method with `checkArgument`/`IllegalArgument`, since the method is invoked
  when a caller attempts to create an immutable instance.
- An Immutables-specific check that disallows references to collection types
  other than the Guava immutable collections, including inside generic type
  arguments.
- An SLF4J-specific check that drops or adds a trailing dot from log messages,
  as applicable.
- A Mockito-specific check that identifies sequences of statements which mock a
  significant number of methods on a single object with "default data"; such
  constructions can often benefit from a different type of default answer, such
  as `Answers.RETURNS_MOCKS`.
- An RxJava-specific check that flags `.toCompletable()` calls on expressions
  of type `Single<Completable>` etc., as most likely
  `.flatMapCompletable(Functions.identity())` was meant instead. Idem for other
  variations.
- An RxJava-specific check that flags `expr.firstOrError()` calls and suggests
  `expr.switchIfEmpty(Single.error(...))`, so that an application-specific
  exception is thrown instead of `NoSuchElementException`.
- An RxJava-specific check that flags use of `#assertValueSet` without
  `#assertValueCount`, as the former method doesn't do what one may intuitively
  expect it to do. See ReactiveX/RxJava#6151.

### Refaster extension ideas

XXX: This section should live elsewhere.

It's much easier to implement a Refaster rule than an Error Prone bug checker,
but on the flip side Refaster is much less expressive. While this gap can never
be fully closed, there are some ways in which Refaster's scope of utility could
be extended. The following is a non-exhaustive list of ideas on how to extend
Refaster's expressiveness:

- Allow more control over _which_ methods are statically imported by
  `@UseImportPolicy`. Sometimes the `@AfterTemplate` contains more than one
  static method invocation, and only a subset should be statically imported.
- Provide a way to express that a lambda expression should also match an
  equivalent method reference and/or vice versa.
- Provide a way to express that certain method invocations should also be
  replaced when expressed as a method reference, or vice versa. (The latter may
  be simpler: perhaps the rule `T::m1` -> `T::m2` can optionally be interpreted
  to also cover `T.m1(..)` -> `T.m2(...)`.)
- Some Refaster refactorings (e.g. when dealing with lazy evaluation) are valid
  only when some free parameter is a constant, variable reference or some other
  pure expression. Introduce a way to express such a constraint. For example,
  rewriting `optional1.map(Optional::of).orElse(optional2)` to `optional1.or(()
  -> optional2)` is not behavior preserving if evaluation of `optional2` has
  side effects.
- Similarly, certain refactoring operations are only valid if one of the
  matched expressions is not `@Nullable`. It'd be nice to be able to express
  this.
- Generalize `@Placeholder` support such that rules can reference e.g. "any
  concrete unary method". This would allow refactorings such as
  `Mono.just(constant).flatmap(this::someFun)` -> `Mono.defer(() ->
  someFun(constant))`.
- Sometimes a Refaster refactoring can cause the resulting code not to compile
  due to a lack of generic type information. Identify and resolve such
  occurrences. For example, an `@AfterTemplate` may require the insertion of a
  statically imported method, which can cause required generic type information
  to be lost. In such a case don't statically import the method, so that the
  generic type information can be retained. (There may be cases where generic
  type information should even be _added_. Find an example.)
- Provide a way to express "match if (not) annotated (with _X_)". See #1 for a
  motivating example.
- Provide a way to place match constraints on compile time constants. For
  example, "match if this integer is less than 2" or "match if this string
  matches the regular expression `X`".
- Provide a way to express transformations of compile-time constants. This
  would allow one to e.g. rewrite single-character strings to chars or vice
  versa, thereby accommodating a target API. Another example would be to
  replace SLF4J's `{}` placeholders with `%s` or vice versa. Yet another
  example would be to rewrite `BigDecimal.valueOf("<some-long-value>")` to
  `BigDecimal.valueOf(theParsedLongValue)`.
- More generally, investigate ways to plug in fully dynamic behavior, e.g.  by
  providing hooks which enable plugging in arbitrary
  predicates/transformations. The result would be a Refaster/`BugChecker`
  hybrid. A feature like this could form the basis for many other features
  listed here. (As a concrete example, consider the ability to reference
  `com.google.errorprone.matchers.Matcher` implementations.)
- Provide an extension API that enables defining methods or expressions based
  on functional properties. A motivating example is the Java Collections
  framework, which allows many ways to define (im)mutable (un)ordered
  collections with(out) duplicates. One could then express things like "match
  any method call that collects its inputs into an immutable ordered list". An
  enum analogous to `java.util.stream.Collector.Characteristics` could be used.
  Out of the box JDK and Guava collection factory methods could be classified,
  with the user having the option to extend the classification.
- Refaster currently unconditionally ignores expressions containing comments.
  Provide two additional modes: (a) match and drop the comments or (b)
  transport the comments to before/after the replaced expression.
- Extend Refaster to drop imports that become unnecessary as a result of a
  refactoring. This e.g. allows one to replace a statically imported TestNG
  `fail(...)` invocation with a statically imported equivalent AssertJ
  `fail(...)` invocation. (Observe that without an import cleanup this
  replacement would cause a compilation error.)
- Extend the `@Repeated` match semantics such that it also covers non-varargs
  methods. For a motivating example see google/error-prone#568.
- When matching explicit type references, also match super types. For a
  motivating example, see the two subtly different loop definitions in
  `CollectionRemoveAllFromCollectionExpression`.
- Figure out why Refaster sometimes doesn't match the correct generic overload.
  See the `AssertThatIterableHasOneComparableElementEqualTo` template for an
  example.

[autorefactor]: https://autorefactor.org
[bettercodehub]: https://bettercodehub.com
[checkstyle-external-project-tests]: https://github.com/checkstyle/checkstyle/blob/master/wercker.yml
[codecov]: https://codecov.io
[error-prone-bug-patterns]: https://errorprone.info/bugpatterns
[error-prone]: https://errorprone.info
[error-prone-repo]: https://github.com/google/error-prone
[forbidden-apis]: https://github.com/policeman-tools/forbidden-apis
[fossa]: https://fossa.io
[google-java-format]: https://github.com/google/google-java-format
[main-contributing]: ../CONTRIBUTING.md
[main-readme]: ../README.md
[modernizer-maven-plugin]: https://github.com/gaul/modernizer-maven-plugin
[sonarcloud]: https://sonarcloud.io
