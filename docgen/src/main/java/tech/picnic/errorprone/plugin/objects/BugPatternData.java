package tech.picnic.errorprone.plugin.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;

/** XXX: Write. */
// XXX: What about `SuppressionAnnotations` and `DocumentSuppression`?
@AutoValue
public abstract class BugPatternData {
  public static BugPatternData create(
      String name,
      String altNames,
      LinkType linkType,
      String link,
      String tags,
      String summary,
      String explanation,
      SeverityLevel severityLevel,
      boolean disableable) {
    return new AutoValue_BugPatternData(
        name, altNames, linkType, link, tags, summary, explanation, severityLevel, disableable);
  }

  @JsonProperty
  abstract String name();

  // XXX: Should be `String[]`.
  @JsonProperty
  abstract String altNames();

  @JsonProperty
  abstract LinkType linkType();

  @JsonProperty
  abstract String link();

  // XXX: Should be `String[]`.
  @JsonProperty
  abstract String tags();

  @JsonProperty
  abstract String summary();

  @JsonProperty
  abstract String explanation();

  @JsonProperty
  abstract SeverityLevel severityLevel();

  @JsonProperty
  abstract boolean disableable();
}
