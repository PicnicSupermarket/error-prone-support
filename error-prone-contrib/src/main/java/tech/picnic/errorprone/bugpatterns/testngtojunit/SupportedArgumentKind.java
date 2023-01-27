package tech.picnic.errorprone.bugpatterns.testngtojunit;

import static java.util.Arrays.stream;

import com.google.errorprone.annotations.Immutable;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.testngtojunit.migrators.DataProviderArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.migrators.DescriptionArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.migrators.ExpectedExceptionsArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.migrators.PriorityArgumentMigrator;

@Immutable
public enum SupportedArgumentKind {
  PRIORITY("priority", new PriorityArgumentMigrator()),
  DESCRIPTION("description", new DescriptionArgumentMigrator()),
  DATAPROVIDER("dataProvider", new DataProviderArgumentMigrator()),
  EXPECTED_EXCEPTIONS("expectedExceptions", new ExpectedExceptionsArgumentMigrator());

  private final String name;

  private final ArgumentMigrator argumentMigrator;

  SupportedArgumentKind(String name, ArgumentMigrator argumentMigrator) {
    this.name = name;
    this.argumentMigrator = argumentMigrator;
  }

  public ArgumentMigrator getArgumentMigrator() {
    return argumentMigrator;
  }

  public static Optional<SupportedArgumentKind> fromString(String argument) {
    return stream(values()).filter(value -> value.name.equals(argument)).findFirst();
  }
}
