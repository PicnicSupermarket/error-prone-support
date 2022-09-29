package tech.picnic.errorprone.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.errorprone.BugPattern.SeverityLevel;

@AutoValue
abstract class RefasterTemplateData {
  static RefasterTemplateData create(
      String name, String description, String link, SeverityLevel severityLevel) {
    return new AutoValue_RefasterTemplateData(name, description, link, severityLevel);
  }

  @JsonProperty
  abstract String name();

  @JsonProperty
  abstract String description();

  @JsonProperty
  abstract String link();

  @JsonProperty
  abstract SeverityLevel severityLevel();
}
