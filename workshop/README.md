# Error Prone Workshop

Download the slides [here][eps-workshop-slides].

## Initial setup of the workshop

1. Start by cloning this repository locally from GitHub.
2. Checkout the `workshop` branch.
3. Make sure to run `mvn clean install` in the root of this repository.
4. Open your code editor and familiarize yourself with the project structure.

In IntelliJ IDEA (or your preferred editor), try to run the
`WorkshopRefasterRulesTest` test. You might see the following error:

```
java: exporting a package from system module jdk.compiler is not allowed with --release
```

If this happens, go to _File -> Settings... -> Build, Execution, Deployment ->
Compiler -> Java Compiler_ and deselect the option _Use '--release' option for
cross-compilation (Java 9 and later)_.

If you encounter any other bugs when running `mvn clean install`, make sure you
are using a recent release of JDK 17.

Now the project is ready for the workshop.

## Part 1: Writing Refaster rules

During this part of the workshop we will implement multiple Refaster rules.

Go to the `workshop` module and navigate to the
`tech.picnic.errorprone.workshop.refasterrules` package. There you can find one
example and five different exercises to do. Make sure to open the
`WorkshopRefasterRulesTest` class where you can enable tests. Per assignment
there is a test in this class that you can enable (by dropping the `@Disabled`
annotation) to validate your changes. The goal is to implement or improve the
Refaster rules such that the enabled tests pass.

Tips:

* Go through the exercises in the proposed order.
* The `XXX:` comments explain what needs to happen.
* Find the test case for each Refaster rule by looking for the name of the
  Refaster rule, prefixed with `test`. For example, the
  `WorkshopAssignment0Rules.java` rule collection has a Refaster rule named
  `ExampleStringIsEmpty`. In the `WorkshopAssignment0RulesTestInput.java` and
  `WorkshopAssignment0RulesTestOutput.java` files there is a
  `testExampleStringIsEmpty` method that shows the input and output to test the
  Refaster rule.


## Part 2: Writing Error Prone checks

During this part of the workshop we will implement parts of multiple Error
Prone `BugChecker`s. Each of these classes contain `XXX` comments explaining
what needs to be implemented. However, before diving in, make sure to first
navigate to a check's associated test class to drop the class-level `@Disabled`
annotation. Upon initial execution the tests will fail; the goal is to get then
to pass.

Some utility classes that you can use:

* `com.google.errorprone.util.ASTHelpers`: contains many common operations on
  the Abstract Syntax Tree.
* `com.google.errorprone.fixes.SuggestedFixes`: contains helper methods for
  creating `Fix`es.

### Tips and tricks

If you are working from the command line, it is suggested to run commands in
the `workshop` directory. To do this, navigate to the root of the repository
and run:
```bash
cd workshop
```

In case you want to do a fast build to just compile the project, run:
```bash
mvn clean install -DskipTests -Dverification.skip
```

In case you want to run one test from the command line, run the following
commands:
```bash
mvn clean install -Dtest=WorkshopRefasterRulesTest -Dverification.skip
```

[eps-github]: https://github.com/PicnicSupermarket/error-prone-support
[eps-workshop-slides]: https://drive.google.com/file/d/19c5675wvRws9e-75jsS-spaD9-Sgw6BQ/view?usp=sharing
