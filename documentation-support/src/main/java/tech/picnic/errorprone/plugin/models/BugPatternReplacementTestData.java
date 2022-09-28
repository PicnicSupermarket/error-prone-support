package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;
import org.jspecify.annotations.Nullable;

@AutoValue
public abstract class BugPatternReplacementTestData {
  public static BugPatternReplacementTestData create(String inputLines, String outputLines) {
    return new AutoValue_BugPatternReplacementTestData(inputLines, outputLines);
  }

  abstract @Nullable String inputLines();

  abstract @Nullable String outputLines();
}
