# Refaster test support

This module provides utilities to validate _Refaster template collections_. A
template collection is a set of Refaster rules represented as static nested
classes, all located in a shared top-level class.

## What does this module do?

These utilities allow for validating the rewrites (or absence of rewrites)
performed by Refaster rules. Each collection of Refaster rules defined in a
single top-level class is applied to an input file, and the resulting rewrites
should match the associated output file.

The validation performed by this module ensures that each Refaster template is
tested, making sure that it matches and transforms code as intended. If a
Refaster template is not covered by a test, if it influences unrelated test
code, or if the associated test doesn't follow certain established standards,
then this irregularity will be reported, and the associated template collection
test will fail. This way, developers receive guidance on how to write Refaster
template tests and assurance that every template is properly tested.

## How to test a collection of Refaster rules

In a nutshell, to test a Refaster template collection using the
`RefasterTemplateCollection` class, one should create suitably named input and
output source code files. The collection's Refaster rules are applied to the
input file, and the generated patches must exactly produce the contents of
the associated output file.

To test Refaster rules, one can create a (parameterized) test for every class
that contains Refaster rules and invoke
`RefasterTemplateCollection#validate`. This test utility applies the Refaster
templates in the collection to a provided input file, and expects the result to
exactly match the contents of a provided output file.

To adopt this setup, the following requirements must be met:

- Each Refaster template collection must match the naming convention
  `<TemplateCollectionName>Templates.java`.
- There is a test class with a (parameterized) test method that invokes
  `RefasterTemplateCollection#validate` on the template collection(s) to be
  validated.
- For every template collection there is an input file matching the naming
  convention `<TemplateCollectionName>TemplatesTestInput.java`.
- For every template collection there is an output file matching the naming
  convention `<TemplateCollectionName>TemplatesTestOutput.java`.
- For every Refaster template in a collection, the associated input and output
  files must contain a method that validates the template's behavior. The name
  of this method must be derived from the name of the Refaster template it aims
  to validate, prefixed with `test` (i.e. `test<RefasterTemplateClassName>`).
- Each such method contains at least one expression that matches the
  `@BeforeTemplate` of the corresponding Refaster template. As a result, the
  output file must contain the same method with an updated expression, in
  accordance with the associated `@AfterTemplate`.
- Such methods must not match any _other_ Refaster rules.

An example directory structure for such a setup is as follows:
```
src/
  main/
    java/
      tech.picnic.errorprone.refastertemplates
      └── ExampleTemplates.java  -- Contains multiple Refaster rules.
          └── Example1Template
          └── Example2Template
  test/
    java/
      tech.picnic.errorprone.refastertemplates
      └── RefasterCollectionTest.java
             -- This test class invokes
             -- `RefasterTemplateCollection#validate`.
    resources/
      tech.picnic.errorprone.refastertemplates
      └── ExampleTemplatesTestInput.java
             -- Contains a class named `ExampleTemplatesTest` and
             -- two methods named `testExample1Template` and
             -- `testExample2Template`.
      └── ExampleTemplatesTestOutput.java
             -- Contains a class named `ExampleTemplatesTest` and
             -- two methods named `testExample1Template` and
             -- `testExample2Template`.
```
