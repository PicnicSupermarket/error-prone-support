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
3. Next, run `mvn versions:set -DnewVersion=0.8.1-testng-migration -DgenerateBackupPoms=false`.
   This will update set the version to `0.8.1-testng-migration`. 
4. Next, run `mvn clean install`. This will create a `0.8.1-testng-migrator` version
   of the `testng-junit-migrator` module. The version will now be available in your local Maven repository.
5. Next, edit the root `pom.xml` of the target module and add the following `testng-migrator`
   profile:

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
                                <version>0.8.1-testng-migration</version>
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
[run-testng-junit-migrator.sh][migration-script] script.

This script will:

1. Run the TestNG tests and count the number of completed tests.
2. Add the required `JUnit` dependencies to your `pom.xml`.
3. Run the `testng-to-junit` migration.
4. Run the migrated JUnit tests and count the number of completed tests.
5. Display the difference in the amount of completed tests.

### Picnic specific
The `PicnicSupermarket/picnic-scratch` repository contains a helper script
`java-platform/testng-junit-migration.sh` that migrates some more
Picnic-specific code. This should be executed _before_ starting the actual
migration.

Continue with performing the actual migration [here](#run-the-migration).
Afterwards, run the `./picnic-shared-tools/patch.sh` script.

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

[error-prone-installation-guide]: https://errorprone.info/docs/installation#maven
[error-prone-orig-repo]: https://github.com/google/error-prone
[eps-readme]: ../README.md
[migration-script]: run-testng-junit-migration.sh
