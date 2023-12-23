package tech.picnic.errorprone.documentation.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.errorprone.BugPattern.SeverityLevel;

// XXX: This class is not yet used.
@AutoValue
@JsonDeserialize(as = AutoValue_RefasterTemplateData.class)
public abstract class RefasterTemplateData {
  static RefasterTemplateData create(
      String name, String description, String link, SeverityLevel severityLevel) {
    return new AutoValue_RefasterTemplateData(name, description, link, severityLevel);
  }

  abstract String name();

  abstract String description();

  abstract String link();

  abstract SeverityLevel severityLevel();
}
