package tech.picnic.errorprone.testngjunit;

import static java.util.Arrays.stream;

/** The annotation attributes that are supported by the TestNG to JUnit Jupiter migration. */
enum TestAnnotationAttribute {
  DATA_PROVIDER("dataProvider", new DataProviderAttributeMigrator()),
  DESCRIPTION("description", new DescriptionAttributeMigrator()),
  ENABLED("enabled", new EnabledAttributeMigrator()),
  EXPECTED_EXCEPTIONS("expectedExceptions", new ExpectedExceptionsAttributeMigrator()),
  GROUPS("groups", new GroupsAttributeMigrator()),
  PRIORITY("priority", new PriorityAttributeMigrator()),
  TIMEOUT("timeOut", new TimeOutAttributeMigrator()),
  UNSUPPORTED("_unsupported", new UnsupportedAttributeMigrator());

  private final String name;
  private final Migrator attributeMigrator;

  TestAnnotationAttribute(String name, Migrator attributeMigrator) {
    this.name = name;
    this.attributeMigrator = attributeMigrator;
  }

  Migrator getAttributeMigrator() {
    return attributeMigrator;
  }

  static TestAnnotationAttribute fromString(String attribute) {
    return stream(values()).filter(v -> v.name.equals(attribute)).findFirst().orElse(UNSUPPORTED);
  }
}
