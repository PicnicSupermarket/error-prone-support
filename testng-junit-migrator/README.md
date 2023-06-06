# TestNG to JUnit Jupiter migrator

This module contains a tool to automatically migrate TestNG tests to JUnit
Jupiter. The tool is built on top of [Error Prone][error-prone-orig-repo]. To
use it, read the installation guide below.

### Installation

1. First, follow Error Prone's [installation
   guide][error-prone-installation-guide]. For extra information, see this
   [README][eps-readme]. (This step can be skipped for Picnic repositories!)
2. Clone the Error Prone Support repository and checkout the branch
   `gdejong/testng-migrator`.
3. Next, run `mvn versions:set -DnewVersion=0.10.1-testng-migration -DgenerateBackupPoms=false`.
   This will update set the version to `0.10.1-testng-migration`.
4. Next, run `mvn clean install`. This will create a `0.10.1-testng-migrator` version
   of the `testng-junit-migrator` module. The version will now be available in your local Maven repository.
5. Finally, add the following profile to your `pom.xml`. This should be the `pom.xml` in the root of your module. 
   Usually this is the parent `pom.xml`, but single module projects are also supported.

```xml
<profiles>
    <profile>
        <id>testng-migrator</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <annotationProcessorPaths combine.children="append">
                            <path>
                                <groupId>tech.picnic.error-prone-support</groupId>
                                <artifactId>testng-junit-migrator</artifactId>
                                <version>0.10.1-testng-migration</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Having this profile allows the migration script to verify the correctness of
the result by making sure the same amount of tests are executed.

## Run the migration

> **Note**
> For Picnic repositories there is an extra step required _before_ running the
> migration, see [here](#picnic-specific).

Now that the migration is set up, one can start the migration by executing the
[run-testng-junit-migrator.sh][migration-script] script in the same directory as the `pom.xml` file we changed earlier.

This script will:

1. Add the required `JUnit` dependencies to your `pom.xml`.
2. Run the `testng-to-junit` migration.

> **Note**
> Please verify that the migrated code still compiles after each step of the compilation.

### Counting tests

The amount of tests executed before the migration can be counted using the `--count` flag:
```sh
./run-testng-junit-migrator.sh --count
```
This will count the amount of tests that are executed. This is recommended before running the migration
to allow for comparison.

### Picnic specific

The `PicnicSupermarket/picnic-scratch` repository contains a helper script
`java-platform/testng-junit-migration.sh` that migrates some more
Picnic-specific code. This should be executed _before_ starting the actual
migration.

> **Warning**
> This is a warning for `macOs` users.
> Make sure gnu-grep and gnu-sed are installed!
>
> ```brew install grep gnu-sed```

Continue with performing the actual migration [here](#run-the-migration).
Afterward, run the `./picnic-shared-tools/patch.sh` script.

Now you are done! 🤘🚀

### Migration code example

Consider the following TestNG test class:

```java
// TestNG code:
@Test
public class A {
  public void simpleTest() {}

  @Test(priority = 2)
  public void priorityTest() {}

  @DataProvider
  private static Object[][] dataProviderTestCases() {
    return new Object[]{{1}, {2}, {3}};
  }

  @Test(dataProvider = "dataProviderTestCases")
  public void dataProviderTest(int number) {}
}
```

This migration tool will turn this into the following:

```java
// JUnit Jupiter code:
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class A {
  @Test
  void simpleTest() {}

  @Test
  @Order(2)
  public void priorityTest() {}

  private static Stream<Argument> dataProviderTestCases() {
    return Stream.of(arguments(1), arguments(2), arguments(3));
  }

  @ParameterizedTest
  @MethodSource("dataProviderTestCases")
  public void dataProviderTest(int number) {}
}
```

### Known limitations
- Certain `@DataProvider` methods cannot be automatically migrated (e.g., `return Stream.of(...).toArray(Object[][]::new)`).
- Some uncommon `@Test` attributes are not supported, such as `ignoreMissingDependencies` and `dependsOnMethods`.
- Test setup and teardown methods `@{Before, After}Test` are migrated to `@{Before, After}Each` to avoid introducing breaking changes. `@{Before, After}All` require a static method, while `@{Before, After}Test` are instance methods.

[eps-readme]: ../README.md
[error-prone-installation-guide]: https://errorprone.info/docs/installation#maven
[error-prone-orig-repo]: https://github.com/google/error-prone
[migration-script]: run-testng-junit-migration.sh
