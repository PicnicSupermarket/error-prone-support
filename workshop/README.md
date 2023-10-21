# Error Prone Workshop

Download the slides [here][eps-workshop-codelabjug].

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

If this happens, go to _Settings -> Build, Execution, Deployment -> Compiler ->
Java Compiler_ and deselect the option _Use '--release' option for
cross-compilation (Java 9 and later)_.

Now the project is ready for the workshop.

## Part 1: Writing Refaster rules

During this part of the workshop we will implement multiple Refaster rules.

Go to the `workshop` module and open the
`tech.picnic.errorprone.workshop.refasterrules` package. There you can find one
example and 5 different exercises to do. Make sure to check out the
`WorkshopRefasterRulesTest.java` class where you can enable tests. Per
assignment there is a test in this class that one can enable (by dropping the
`@Disabled` annotation) to validate your changes. The goal is to implement or
improve the Refaster rules such that the enabled tests pass.

Tips:

* Go through the exercises in the proposed order.
* The `XXX:` comments explains what needs to happen.
* See the test cases for each Refaster rule by looking for the name of the
  Refaster rule prefixed with `test`. For example, the
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

## Part 3 (optional): Validating changes with the integration tests

We created an integration testing framework that allows us to see the impact of
the rules that are created. This testing framework can be executed locally and
via GitHub Actions.

If you still have more than 10-15 minutes left, and you want to test this
locally, run the following commands:

```sh
mvn clean install -DskipTests -Dverification.skip
./integration-tests/checkstyle-10.12.4.sh
```

Once the process is complete, and changes are introduced, the following output
will be printed:

```
There are unexpected changes.
Inspect the changes here: /tmp/tmp.Cmr423L1pA/checkstyle-10.12.4-diff-of-diffs-changes.patch
```

This file is provided for your review using your preferred text editor.
Alternatively, you can also navigate to the repository by going to the
`./integration-tests/.repos/checkstyle` directory and executing `git log -p` to
view the commit history and associated changes.

This shows the impact of the rules that you wrote when they are applied to
Checkstyle!

The other option is to execute the integration test via GitHub Actions. You
only need to commit and push to the branch. This will trigger execution of the
integration tests, which will run for about 10 minutes. When the build is
finished, go to the _Actions_ tab in your fork and navigate to your most recent
commit and click on it. Then click on _Summary_ and download the artifact
`integration-test-checkstyle-10.12.4` at the bottom. Once done, unzip the
artifact and inspect the `checkstyle-10.12.4-diff-of-diffs-changes.patch` file
to see the changes.

[eps-github]: https://github.com/PicnicSupermarket/error-prone-support
[eps-workshop-codelabjug]: https://drive.google.com/file/d/1Q9HD5rKrcFszonOGqPqa2p7vyvV30brg/view
