package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.google.auto.value.AutoValue;
import com.sun.source.tree.ClassTree;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.DataProvider;

/** Data class containing information on the current state of a TestNG -> JUnit migration */
@AutoValue
public abstract class TestNGMigrationContext {

  abstract boolean isConservativeMode();

  /**
   * Get the {@link ClassTree} for the current migration scope.
   *
   * @return the class tree
   */
  public abstract ClassTree getClassTree();

  /**
   * Get a mapping of a {@link org.testng.annotations.DataProvider}'s name to their {@link
   * MigrationState}
   *
   * @return a map of dataprovider names to their migration state
   */
  public abstract Map<String, MigrationState> getMigratedDataProviders();

  /**
   * Instantiate a new {@link TestNGMigrationContext}.
   *
   * @param conservativeMode whether to run the migration in conservative mode
   * @param classTree the class tree this context will be used for
   * @return a new {@link TestNGMigrationContext} instance
   */
  public static TestNGMigrationContext create(boolean conservativeMode, ClassTree classTree) {
    return new AutoValue_TestNGMigrationContext(conservativeMode, classTree, new HashMap<>());
  }

  /** The migration states for a {@link org.testng.annotations.DataProvider}. */
  public enum MigrationState {
    /** {@link DataProvider} hasn't been migrated yet */
    NOT_MIGRATED,
    /** {@link DataProvider} has been migrated */
    MIGRATED,
    /** Cannot migrate {@link DataProvider} */
    CANNOT_MIGRATE;
  }
}
