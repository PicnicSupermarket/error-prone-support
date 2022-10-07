package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class BugPatternTestData {
  public static BugPatternTestData create(
      String name,
      List<String> identificationTests,
      List<BugPatternReplacementTestData> replacementTests) {
    return new AutoValue_BugPatternTestData(name, identificationTests, replacementTests);
  }

  abstract String name();

  @Nullable
  abstract List<String> identificationTests();

  @Nullable
  abstract List<BugPatternReplacementTestData> replacementTests();
}
