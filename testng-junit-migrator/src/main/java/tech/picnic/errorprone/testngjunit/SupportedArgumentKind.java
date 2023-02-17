package tech.picnic.errorprone.testngjunit;

import static java.util.Arrays.stream;

import java.util.Optional;
import tech.picnic.errorprone.testngjunit.migrators.argument.DataProviderArgumentMigrator;
import tech.picnic.errorprone.testngjunit.migrators.argument.DescriptionArgumentMigrator;
import tech.picnic.errorprone.testngjunit.migrators.argument.ExpectedExceptionsArgumentMigrator;
import tech.picnic.errorprone.testngjunit.migrators.argument.PriorityArgumentMigrator;

/** The annotation argument kinds that are supported by the TestNG -> JUnit migration. */
enum SupportedArgumentKind {
  PRIORITY("priority", new PriorityArgumentMigrator()),
  DESCRIPTION("description", new DescriptionArgumentMigrator()),
  DATAPROVIDER("dataProvider", new DataProviderArgumentMigrator()),
  EXPECTED_EXCEPTIONS("expectedExceptions", new ExpectedExceptionsArgumentMigrator());

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
