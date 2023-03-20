# TestNG to JUnit Jupiter migrator

A tool to automatically migrate TestNG tests to JUnit Jupiter.

### Example

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

### Installation

First edit your `pom.xml` and add `testng-junit-migrator` to the
`annotationProcessorPaths` of the `maven-compiler` plugin:

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

The `testng-migrator` profile isn't required, but it will allow the
`run-testng-junit-migrator.sh` script to count the amount of tests that are run
before and after the migration.

Next, to run the migrator use the following command:

```sh
mvn \
  -Perror-prone \
  -Ptestng-migrator \
  -Ppatch \
  clean test-compile fmt:format \
  -Derror-prone.patch-checks="TestNGJUnitMigration" \
  -Dverification.skip
```

There's a script to easily invoke this command `run-testng-junit-migrator.sh`.
This script will:

1. Run the TestNG tests and count the number of completed tests.
2. Add the required `JUnit` dependencies to your `pom.xml`.
3. Run the `testng-to-junit` migration.
4. Run the migrated JUnit tests and count the number of completed tests.
5. Display the difference in the amount of completed tests.

### Picnic specific
`picnic-scratch` contains a small helper script `java-platform/testng-junit-migration.sh` that migrates some
more Picnic-specific code.
