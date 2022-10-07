package tech.picnic.errorprone.plugin.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class BugPatternTestData {
  public static BugPatternTestData create(
      String name,
      List<String> identificationLines,
      List<String> inputLines,
      List<String> outputLines) {
    return new AutoValue_BugPatternTestData(name, identificationLines, inputLines, outputLines);
  }

  @JsonProperty
  abstract String name();

  @Nullable
  @JsonProperty
  abstract List<String> identificationLines();

  @Nullable
  @JsonProperty
  abstract List<String> inputLines();

  @Nullable
  @JsonProperty
  abstract List<String> outputLines();
}
