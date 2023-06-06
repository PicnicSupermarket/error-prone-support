package tech.picnic.errorprone.testngjunit;

import static java.util.Arrays.stream;

import java.util.Optional;

/** The annotation attributes that are supported by the TestNG to JUnit Jupiter migration. */
enum TestAnnotationAttribute {
  DATAPROVIDER("dataProvider", new DataProviderAttributeMigrator()),
  DESCRIPTION("description", new DescriptionAttributeMigrator()),
  ENABLED("enabled", new EnabledAttributeMigrator()),
  EXPECTED_EXCEPTIONS("expectedExceptions", new ExpectedExceptionsAttributeMigrator()),
  GROUPS("groups", new GroupsAttributeMigrator()),
  PRIORITY("priority", new PriorityAttributeMigrator()),
  TIMEOUT("timeOut", new TimeOutAttributeMigrator());

  private final String name;
  private final Migrator attributeMigrator;

  TestAnnotationAttribute(String name, Migrator attributeMigrator) {
    this.name = name;
    this.attributeMigrator = attributeMigrator;
  }

  Migrator getAttributeMigrator() {
    return attributeMigrator;
  }

  static Optional<TestAnnotationAttribute> fromString(String attribute) {
    return stream(values()).filter(v -> v.name.equals(attribute)).findFirst();
  }
}
