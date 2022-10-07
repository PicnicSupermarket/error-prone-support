package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class BugPatternReplacementTestData {
  public static BugPatternReplacementTestData create(String inputLines, String outputLines) {
    return new AutoValue_BugPatternReplacementTestData(inputLines, outputLines);
  }

  @Nullable
  abstract String inputLines();

  @Nullable
  abstract String outputLines();
}
