package tech.picnic.errorprone.bugpatterns.testmigrator;

import static java.util.Arrays.stream;

import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.DataProviderArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.DescriptionArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.PriorityArgumentMigrator;

public enum SupportedArgumentKind {
  PRIORITY("priority", new PriorityArgumentMigrator()),
  DESCRIPTION("description", new DescriptionArgumentMigrator()),
  DATAPROVIDER("dataProvider", new DataProviderArgumentMigrator());
  private final String name;

  @SuppressWarnings("ImmutableEnumChecker" /* `SupportedArgumentKind` is effectively immutable. */)
  private final ArgumentMigrator fixer;

  SupportedArgumentKind(String name, ArgumentMigrator fixer) {
    this.name = name;
    this.fixer = fixer;
  }

  public ArgumentMigrator getFixer() {
    return fixer;
  }

  public static boolean canMigrateTest(TestNGMetadata.TestNGAnnotation annotation) {
    return annotation.getArgumentNames().stream()
        .map(SupportedArgumentKind::matchArgument)
        .allMatch(Optional::isPresent);
  }

  public static Optional<SupportedArgumentKind> matchArgument(String argument) {
    return stream(values()).filter(value -> value.name.equals(argument)).findFirst();
  }
}
