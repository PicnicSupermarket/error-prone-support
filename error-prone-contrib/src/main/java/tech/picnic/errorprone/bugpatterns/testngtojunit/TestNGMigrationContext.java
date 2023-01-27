package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.sun.source.tree.ClassTree;
import java.util.HashMap;
import java.util.Map;

// XXX: Same goes for this class, it should be using `@AutoValue`. See my other comment about this
// for context.
public final class TestNGMigrationContext {
  private final boolean aggressiveMigration;
  private final ClassTree classTree;
  private final Map<String, MigrationState> migratedDataProviders = new HashMap<>();

  public TestNGMigrationContext(boolean aggressiveMigration, ClassTree classTree) {
    this.aggressiveMigration = aggressiveMigration;
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

  public boolean isAggressiveMigration() {
    return aggressiveMigration;
  }

  public enum MigrationState {
    NOT_MIGRATED,
    MIGRATED,
    CANNOT_MIGRATE;
  }
}
