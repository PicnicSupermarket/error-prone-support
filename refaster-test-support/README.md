# Refaster test support

This package provides utilities to validate Refaster template collections.

## What does this module do?

These utilities allow for validating the rewrites performed by Refaster templates, or absence thereof. Each collection
of Refaster templates is applied to an input file and the result of that should match the provided output file.

This extension ensures that each Refaster template has a test and has a match at the expected place. If a test is
missing, an error is reported in the test's console. This way, the developer has some guidance on how to write tests and
assurance that every template is tested.

## How to test a collection of Refaster templates?

In summary, to test Refaster templates using the `RefasterCollectionTestUtil`, one should create an input and output
file. The Refaster templates from the collection are applied on the input file and should exactly match the content of
the provided output file.

To test Refaster templates, one can create a (parameterized) test for every class containing the Refaster templates to
invoke the `RefasterCollectionTestUtil`. A class that contains one or more Refaster templates is called a Refaster
template collection. This test utility applies the Refaster templates in the collection to a provided input file, and
expects the result to exactly match the contents of a provided output file.

To adopt this setup, the following requirements have to be met:

- Create a class with a (parameterized) test method that calls
  the `RefasterCollectionTestUtil#validateTemplateCollection` and passes the collection(s) to validate. The Refaster
  template collection must match the naming convention `<TemplateCollectionClassName>Templates.java`.
- An input file matching the naming convention `<TemplateCollectionClassName>TemplatesTestInput.java` is added for every
  template collection.
- An output file matching the naming convention `<TemplateCollectionClassName>TemplatesTestOutput.java`
  file is added for every template collection.
- For each Refaster template in the collection, the input and output file contain a method. The name of the method is
  equal to the name of the Refaster template prefixed with `test` (e.g. `test<RefasterTemplateClassName>`).
- The method contains at least one expression that matches the `@BeforeTemplate`. As a result, the output file contains
  the same method with an updated expression, matching the content of the `@AfterTemplate`.

As a result from these tests, unexpected output will be shown in the console.

An example of a folder structure for such a setup:

```
main/java/ 
      tech.picnic.errorprone.refastertemplates
      └── ExampleTemplates.java  -- Contains multiple Refaster templates
          └── Example1Template
          └── Example2Template

test/
  java/
    └── tech.picnic.errorprone.refastertemplates
        └── RefasterCollectionTest.java -- Here the test invokes `RefasterCollectionTestUtil#validateTemplateCollection`.
  resources/
    └── tech.picnic.errorprone.refastertemplates
        └── ExampleTemplatesTestInput.java -- Contains a class named `ExampleTemplatesTest`
        └── ExampleTemplatesTestOutput.java -- Contains a class named `ExampleTemplatesTest`
```