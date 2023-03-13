package tech.picnic.errorprone.testngjunit;

import static java.util.Arrays.stream;

import java.util.Optional;

/** The annotation argument kinds that are supported by the TestNG to JUnit Jupiter migration. */
enum SupportedArgumentKind {
  PRIORITY("priority", new PriorityArgumentMigrator()),
  DESCRIPTION("description", new DescriptionArgumentMigrator()),
  DATAPROVIDER("dataProvider", new DataProviderArgumentMigrator()),
  EXPECTED_EXCEPTIONS("expectedExceptions", new ExpectedExceptionsArgumentMigrator()),
  ENABLED("enabled", new EnabledArgumentMigrator()),
  GROUPS("groups", new GroupsArgumentMigrator());
  private final String name;

  private final Migrator argumentMigrator;

  SupportedArgumentKind(String name, Migrator argumentMigrator) {
    this.name = name;
    this.argumentMigrator = argumentMigrator;
  }

  Migrator getArgumentMigrator() {
    return argumentMigrator;
  }

  static Optional<SupportedArgumentKind> fromString(String argument) {
    return stream(values()).filter(value -> value.name.equals(argument)).findFirst();
  }
}
