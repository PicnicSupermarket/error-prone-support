package tech.picnic.errorprone.plugin.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class BugPatternReplacementTestData {
  public static BugPatternReplacementTestData create(String inputLines, String outputLines) {
    return new AutoValue_BugPatternReplacementTestData(inputLines, outputLines);
  }

  @Nullable
  @JsonProperty
  abstract String inputLines();

  @Nullable
  @JsonProperty
  abstract String outputLines();
}
