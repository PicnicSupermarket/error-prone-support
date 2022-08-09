# Refaster test support

This module provides utilities to validate _Refaster template collections_. A
template collection is a set of Refaster templates represented as static nested
classes, all located in a shared top-level class.

## What does this module do?

These utilities allow validating the rewrites (or their absence) performed by
Refaster templates. Each collection of Refaster templates defined in a single
top-level class is applied to an input file, and the resulting rewrites should
match the associated output file.

The validation performed by this module ensures that each Refaster template is
tested, making sure that it matches and transforms code as intended. If a
Refaster template is not covered by a test, if it influences unrelated test
code, or if the associated test doesn't follow certain established standards,
then this irregularity will be reported, and the associated template collection
test will fail. This way, developers receive guidance on how to write Refaster
template tests and assurance that every template is properly tested.

## How to test a collection of Refaster templates

In a nutshell, to test a Refaster template collection class using the
`RefasterTemplateCollection` class, one should create suitably named input and
output files. The collection's Refaster templates are applied to the input
file, and the generated patches must exactly produce the contents of the
associated output file.

To test Refaster templates, one can create a (parameterized) test for every
class that contains Refaster templates and invoke
`RefasterTemplateCollection#validate`. This test utility applies the Refaster
templates in the collection to a provided input file, and expects the result to
exactly match the contents of a provided output file.

To adopt this setup, the following requirements must be met:

- A class with a (parameterized) test method that invokes
  `RefasterTemplateCollection#validate` is created which provides the
  collection(s) to validate. The Refaster template collection must match the
  naming convention `<TemplateCollectionName>Templates.java`.
- An input file matching the naming convention
  `<TemplateCollectionName>TemplatesTestInput.java` is added for every template
  collection.
- An output file matching the naming convention
  `<TemplateCollectionName>TemplatesTestOutput.java` file is added for every
  template collection.
- For every Refaster template in the collection, the input and output file must
  contain a method. The name of the method is equal to the name of the Refaster
  template prefixed with `test` (e.g. `test<RefasterTemplateInnerClassName>`).
- The method contains at least one expression that matches the
  `@BeforeTemplate` of the corresponding Refaster template. As a result, the
  output file contains the same method with an updated expression, matching the
  content of the `@AfterTemplate`. Additionally, incorrect matches of _other_
  Refaster templates in the method are flagged.

As a result from these tests, unexpected output will be shown in the console.

An example directory structure for such a setup is as follows:
```
src/
  main/
    java/
      tech.picnic.errorprone.refastertemplates
      └── ExampleTemplates.java  -- Contains multiple Refaster templates.
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
