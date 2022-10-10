package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;

/** XXX: Write. */
// XXX: What about `SuppressionAnnotations` and `DocumentSuppression`?
@AutoValue
public abstract class BugPatternData {
  public static BugPatternData create(
      String fullyQualifiedName,
      String name,
      ImmutableList<String> altNames,
      LinkType linkType,
      String link,
      ImmutableList<String> tags,
      String summary,
      String explanation,
      SeverityLevel severityLevel,
      boolean disableable) {
    return new AutoValue_BugPatternData(
        fullyQualifiedName,
        name,
        altNames,
        linkType,
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

  abstract LinkType linkType();

  abstract String link();

  abstract ImmutableList<String> tags();

  abstract String summary();

  abstract String explanation();

  abstract SeverityLevel severityLevel();

  abstract boolean disableable();
}
