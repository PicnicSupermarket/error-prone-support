package tech.picnic.errorprone.plugin.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class BugPatternTestData {
  public static BugPatternTestData create(
      String name, String identificationLines, String inputLines, String outputLines) {
    return new AutoValue_BugPatternTestData(name, identificationLines, inputLines, outputLines);
  }

  @JsonProperty
  abstract String name();

  @Nullable
  @JsonProperty
  abstract String identificationLines();

  @Nullable
  @JsonProperty
  abstract String inputLines();

  @Nullable
  @JsonProperty
  abstract String outputLines();
}
