# Picnic's Error Prone Contrib

This project provides a plugin containing a collection of [Error
Prone][error-prone] checks.

## How to contribute

Contributions are more than welcome! Below we list tasks that are on our TODO
list. If you have others ideas/plans, feel free to file an issue or open a pull
request.

### Contribution guidelines

To the extend possible the pull request process guards our coding guidelines.
Some pointers:
- Checks should we _topical_: Ideally they address a single concern.
- Where possible checks should provide _fixes_, and ideally these are
  completely behavior preserving. In order for a check to be adopted by users
  it must not "get in the way". So for a check which addresses a relatively
  trivial stylistic concern it is doubly important that the violations it
  detects can be auto-patched.
- Make sure you have read Error Prone's [criteria for new
  checks][error-prone-criteria]. Most guidelines described there apply to this
  project as well, except that this project _does_ focus quite heavy on style
  enforcement. But that just makes the previous point doubly important.
- Make sure that a checks's (mutation) coverage is or remains about as high as
  it can be. Not only does this lead to better tests, it also points out
  opportunities to simplify the code.

### Our wishlist

We expect the following tasks to help improve the quality of this open source
project:

- Publish the artifact to Maven Central, then document the coordinates in this
  `README.md`.
- Document how to enable the checks.
- Document how to apply patches.
- Document each of the checks.
- Add Travis CI, [SonarQube][sonarcloud] and [Coveralls][coveralls]
  integrations.
- Validate formatting upon PR builds.
- Investigate whether it makes sense to include license headers in each file.
  If so, set that up and enforce it.
- Add non-Java file formatting support, like we have internally at Picnic.
  (I.e., somehow open-source that stuff.)
- Add relevant "badges" at the top of this `README.md`.
- Auto-generate a website listing each of the checks, just like the Error Prone
  [bug patterns page][error-prone-bug-patterns]. The [Error Prone
  repository][error-prone-repo] contains code for this.
- Set up a script/procedure for testing the plugin against other open source
  projects. By running the checks against a variety of code bases we are more
  likely to catch errors due to unexpected code constructs. False positives and
  regressions can be caught this way as well.
- Create a tool which converts a collection of Refaster Templates into an Error
  Prone check. Ideally this tool is contributed upstream.
- Improve an existing check (see `XXX`-marked comments in the code) or write a
  new one (see the list of suggestions below.)

### Ideas for new checks

The following is a list of checks we'd like to see implemented:

- A check with functionality equivalent to the "[Policeman's Forbidden API
  Checker][forbidden-apis]" Maven plugin. Using method matchers forbidden
  method calls can easily be identified. But Error Prone can go one step
  further by auto-patching violations. For each violation two fixes can be
  proposed: a purely behavior-preserving fix which make the platform-dependent
  behavior explicit, and another which replaces the platform-dependent behavior
  with the preferred alternative. (Such as using `UTF-8` instead of the system
  default charset.)
- A check which replaces fully qualified types with simple types in context
  where this does not introduce ambiguity.
- A check which simplifies array expressions. It would replace empty array
  expressions of the form `new int[] {}` with `new int[0]`. Statements of the
  form `byte[] arr = new byte[] {'c'};` would be shortened to `byte[] arr =
  {'c'};`.
- A check which replaces expressions of the form `String.format("some prefix
  %s", arg)` with `"some prefix " + arg`, and similar for simple suffixes. Can
  perhaps be generalized further, though it's unclear how far. (Well, a
  `String.format` call without arguments can certainly be simplified, too.)
- A check which replaces single-character strings with `char`s where possible.
  For example as argument to `StringBuilder.append` and in string
  concatenations.
- A check which adds or removes the first `Locale` argument to `String.format`
  and similar calls as necessary. (For example, a format string containing only
  `%s` placeholders is local-insensitive unless any of the arguments is a
  `Formattable`, while `%f` placeholders _are_ locale-sensitive.)
- A check which replaces `String.replaceAll` with `String.replace` if the first
  argument is certainly not a regular expression. And if both arguments are
  single-character strings then the `(char, char)` overload can be invoked
  instead.
- A check which flags (and ideally, replaces) `try-finally` constructs with
  equivalent `try-with-resources` constructs.
- A Guava-specific check which replaces `Joiner.join` calls with `String.join`
  calls in those cases where the latter is a proper substitute for the former.
- A Spring-specific check which enforces that methods with the `@Scheduled`
  annotation are also annotated with New Relic's `@Trace` annotation.
- A Spring-specific check which enforces that `@RequestMapping` annotations,
  when applied to a method, explicitly specify one or more target HTTP methods.
- A Spring-specific check which looks for classes in which all
  `@RequestMapping` annotations (and the various aliases) specify the same
  `path`/`value` property and then moves that path to the class level.
- A Spring-specific check which flags `@Value("some.property")` annotations, as
  these almost certainly should be `@Value("${some.property}")`.
- A Spring-specific check which drops the `required` attribute from
  `@RequestParam` annotations when the `defaultValue` attribute is also
  specified.
- A Spring-specific check which rewrites a class which uses field injection to
  one which uses constructor injection. This check wouldn't be strictly
  behavior preserving, but could be used for a one-off code base migration.
- A Spring-specific check which disallows field injection, except in
  `AbstractTestNGSpringContextTests` subclasses.
- A Hibernate Validator-specific check which looks for `@UnwrapValidatedValue`
  usages and migrates the associated constraint annotations to the generic type
  argument to which they (are presumed to) apply.
- A TestNG-specific check which drops method-level `@Test` annotations if a
  matching/more specific annotation is already present at the class level.
- A TestNG-specific check which enforces that all tests are in a group.

[coveralls]: https://coveralls.io
[error-prone-bug-patterns]: http://errorprone.info/bugpatterns
[error-prone-criteria]: http://errorprone.info/docs/criteria
[error-prone]: http://errorprone.info
[error-prone-repo]: https://github.com/google/error-prone
[forbidden-apis]: https://github.com/policeman-tools/forbidden-apis
[sonarcloud]: https://sonarcloud.io
