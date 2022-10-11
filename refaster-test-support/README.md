# Refaster test support

This module provides utilities to validate _Refaster rule collections_. A rule
collection is a set of Refaster rules represented as static nested classes, all
located in a shared top-level class.

## What does this module do?

These utilities allow for validating the rewrites (or absence of rewrites)
performed by Refaster rules. Each collection of Refaster rules defined in a
single top-level class is applied to an input file, and the resulting rewrites
should match the associated output file.

The validation performed by this module ensures that each Refaster rule is
tested, making sure that it matches and transforms code as intended. If a
Refaster rule is not covered by a test, if it influences unrelated test code,
or if the associated test doesn't follow certain established standards, then
this irregularity will be reported, and the associated rule collection test
will fail. This way, developers receive guidance on how to write Refaster rule
tests and assurance that every rule is properly tested.

## How to test a collection of Refaster rules

In a nutshell, to test a Refaster rule collection using the
`RefasterRuleCollection` class, one should create suitably named input and
output source code files. The collection's Refaster rules are applied to the
input file, and the generated patches must exactly produce the contents of the
associated output file.

To test Refaster rules, one can create a (parameterized) test for every class
that contains Refaster rules and invoke `RefasterRuleCollection#validate`. This
test utility applies the Refaster rules in the collection to a provided input
file, and expects the result to exactly match the contents of a provided output
file.

To adopt this setup, the following requirements must be met:

- Each Refaster rule collection must match the naming convention
  `<RuleCollectionName>Rules.java`.
- There is a test class with a (parameterized) test method that invokes
  `RefasterRuleCollection#validate` on the rule collection(s) to be validated.
- For every rule collection there is an input file matching the naming
  convention `<RuleCollectionName>RulesTestInput.java`.
- For every rule collection there is an output file matching the naming
  convention `<RuleCollectionName>RulesTestOutput.java`.
- For every Refaster rule in a collection, the associated input and output
  files must contain a method that validates the rules' behavior. The name of
  this method must be derived from the name of the Refaster rule it aims to
  validate, prefixed with `test` (i.e. `test<RefasterRuleClassName>`).
- Each such method contains at least one expression that matches the
  `@BeforeTemplate` of the corresponding Refaster rule. As a result, the output
  file must contain the same method with an updated expression, in accordance
  with the associated `@AfterTemplate`.
- Such methods must not match any _other_ Refaster rules.

An example directory structure for such a setup is as follows:
```
src/
  main/
    java/
      tech.picnic.errorprone.refasterrules
      └── ExampleRules.java  -- Contains multiple Refaster rules.
          └── Example1Rule
          └── Example2Rule
  test/
    java/
      tech.picnic.errorprone.refasterrules
      └── RefasterCollectionTest.java
             -- This test class invokes
             -- `RefasterRuleCollection#validate`.
    resources/
      tech.picnic.errorprone.refasterrules
      └── ExampleRulesTestInput.java
             -- Contains a class named `ExampleRulesTest` and
             -- two methods named `testExample1Rule` and
             -- `testExample2Rule`.
      └── ExampleRulesTestOutput.java
             -- Contains a class named `ExampleRulesTest` and
             -- two methods named `testExample1Rule` and
             -- `testExample2Rule`.
```
