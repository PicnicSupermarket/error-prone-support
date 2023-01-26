package tech.picnic.errorprone.bugpatterns.testmigrator;

import static java.util.Arrays.stream;

import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.DataProviderArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.DescriptionArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.ExpectedExceptionsArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testmigrator.migrators.PriorityArgumentMigrator;

public enum SupportedArgumentKind {
  PRIORITY("priority", new PriorityArgumentMigrator()),
  DESCRIPTION("description", new DescriptionArgumentMigrator()),
  DATAPROVIDER("dataProvider", new DataProviderArgumentMigrator()),
  EXPECTED_EXCEPTIONS("expectedExceptions", new ExpectedExceptionsArgumentMigrator());
  private final String name;

  @SuppressWarnings("ImmutableEnumChecker" /* `SupportedArgumentKind` is effectively immutable. */)
  private final ArgumentMigrator argumentMigrator;

  SupportedArgumentKind(String name, ArgumentMigrator argumentMigrator) {
    this.name = name;
    this.argumentMigrator = argumentMigrator;
  }

  public ArgumentMigrator getArgumentMigrator() {
    return argumentMigrator;
  }

  // XXX: Move this logic elsewhere?
  public static boolean canMigrateTest(
      TestNGMigrationContext context, TestNGMetadata.TestNGAnnotation annotation) {
    return annotation.getArgumentNames().stream()
        .map(SupportedArgumentKind::matchArgument)
        .filter(Optional::isPresent)
        .allMatch(
            optKind -> optKind.orElseThrow().getArgumentMigrator().canFix(context, annotation));
  }

  public static Optional<SupportedArgumentKind> matchArgument(String argument) {
    return stream(values()).filter(value -> value.name.equals(argument)).findFirst();
  }
}
