package tech.picnic.errorprone.plugin.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonProperty
  abstract String name();

  @Nullable
  @JsonProperty
  abstract List<String> identificationTests();

  @Nullable
  @JsonProperty
  abstract List<BugPatternReplacementTestData> replacementTests();
}
