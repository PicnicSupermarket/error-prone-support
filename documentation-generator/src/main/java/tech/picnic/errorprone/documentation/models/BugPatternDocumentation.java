package tech.picnic.errorprone.documentation.models;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern.SeverityLevel;

/** A class that contains information related to a {@code BugChecker}. */
// XXX: What about `SuppressionAnnotations` and `DocumentSuppression`?
@AutoValue
public abstract class BugPatternDocumentation {
  /** Instantiates a new {@link BugPatternDocumentation} instance. */
  public BugPatternDocumentation() {}

  /**
   * Creates an instance of {@link BugPatternDocumentation}.
   *
   * @param fullyQualifiedName The FQN of the {@code BugChecker}.
   * @param name The simple name of the {@code BugChecker}.
   * @param altNames Alternative names of the {@code BugChecker}.
   * @param link The link to the {@code BugChecker}.
   * @param tags The tags describing the {@code BugChecker}.
   * @param summary The summary of the {@code BugChecker}.
   * @param explanation The explanation of the {@code BugChecker}.
   * @param severityLevel The {@code SeverityLevel} provided in {@code @BugPattern}.
   * @param disableable Describes whether the check can be disabled using command-line flags.
   * @return A non-{@code null} {@code BugPattern}.
   */
  public static BugPatternDocumentation create(
      String fullyQualifiedName,
      String name,
      ImmutableList<String> altNames,
      String link,
      ImmutableList<String> tags,
      String summary,
      String explanation,
      SeverityLevel severityLevel,
      boolean disableable) {
    return new AutoValue_BugPatternDocumentation(
        fullyQualifiedName,
        name,
        altNames,
        link,
        tags,
        summary,
        explanation,
        severityLevel,
        disableable);
  }

  abstract String fullyQualifiedName();

  abstract String name();

  abstract ImmutableList<String> altNames();

  abstract String link();

  abstract ImmutableList<String> tags();

  abstract String summary();

  abstract String explanation();

  abstract SeverityLevel severityLevel();

  abstract boolean disableable();
}
