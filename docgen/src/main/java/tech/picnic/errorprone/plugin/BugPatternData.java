package tech.picnic.errorprone.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import java.util.Arrays;

@AutoValue
abstract class BugPatternData {
  static BugPatternData create(BugPattern annotation, String name) {
    return new AutoValue_BugPatternData(
        name,
        Arrays.toString(annotation.altNames()),
        annotation.linkType(),
        annotation.link(),
        Arrays.toString(annotation.tags()),
        annotation.summary(),
        annotation.explanation(),
        annotation.severity(),
        annotation.disableable());
  }

  @JsonProperty
  abstract String name();

  // Should be String[]
  @JsonProperty
  abstract String altNames();

  @JsonProperty
  abstract LinkType linkType();

  @JsonProperty
  abstract String link();

  @JsonProperty
  // Should be String[]
  abstract String tags();

  @JsonProperty
  abstract String summary();

  @JsonProperty
  abstract String explanation();

  @JsonProperty
  abstract SeverityLevel severityLevel();

  @JsonProperty
  abstract boolean disableable();

  // SuppressionAnnotations?
  // DocumentSuppression?
}
