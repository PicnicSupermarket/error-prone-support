# TestNG to JUnit Jupiter migrator

This module contains a tool to automatically migrate TestNG tests to JUnit
Jupiter. The tool is built on top of [Error Prone][error-prone-orig-repo]. To
use it, read the installation guide below.

### Installation

1. First, follow Error Prone's [installation
   guide][error-prone-installation-guide]. For extra information, see this
   [README][eps-readme].
2. Next, edit your `pom.xml` and add the following `testng-migrator` profile:

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
                                <version>${version.error-prone-support}</version>
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

Now that the migration is set up, one can start the migration by executing the
[run-testng-junit-migrator.sh][migration-script] script.

This script will:

1. Run the TestNG tests and count the number of completed tests.
2. Add the required `JUnit` dependencies to your `pom.xml`.
3. Run the `testng-to-junit` migration by invoking the script.
4. Run the migrated JUnit tests and count the number of completed tests.
5. Display the difference in the amount of completed tests.

### Picnic Specific
The `PicnicSupermarket/picnic-scratch` repository contains a helper script
`java-platform/testng-junit-migration.sh` that migrates some more
Picnic-specific code. This should be executed _before_ starting the actual
migration.

When the migration is done, make sure to run the `picnic-shared-tools/patch.sh`
script.

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

[error-prone-orig-repo]: https://github.com/google/error-prone
[eps-readme]: error-prone-support/README.md
[migration-script]: error-prone-support/testng-junit-migrator/run-testng-junit-migration.sh
