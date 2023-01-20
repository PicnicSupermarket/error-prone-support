package tech.picnic.errorprone.bugpatterns.testmigrator;

import com.sun.source.tree.ClassTree;
import java.util.HashMap;
import java.util.Map;

public class TestNGMigrationContext {
  private final ClassTree classTree;
  private final Map<String, MigrationState> migratedDataProviders = new HashMap<>();

  public TestNGMigrationContext(ClassTree classTree) {
    this.classTree = classTree;
  }

  public ClassTree getClassTree() {
    return classTree;
  }

  public MigrationState getDataProviderMigrationState(String dataProviderName) {
    return migratedDataProviders.getOrDefault(dataProviderName, MigrationState.NOT_MIGRATED);
  }

  public void setDataProviderMigrationState(String dataProviderName, MigrationState state) {
    migratedDataProviders.put(dataProviderName, state);
  }

  public enum MigrationState {
    NOT_MIGRATED,
    MIGRATED,
    CANNOT_MIGRATE;
  }
}
