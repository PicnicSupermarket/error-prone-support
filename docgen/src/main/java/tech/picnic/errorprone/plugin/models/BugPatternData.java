package tech.picnic.errorprone.plugin.models;

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

  abstract String name();

  // XXX: Should be `String[]`.
  abstract String altNames();

  abstract LinkType linkType();

  abstract String link();

  // XXX: Should be `String[]`.
  abstract String tags();

  abstract String summary();

  abstract String explanation();

  abstract SeverityLevel severityLevel();

  abstract boolean disableable();
}
