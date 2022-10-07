package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;
import com.google.errorprone.BugPattern.SeverityLevel;

@AutoValue
public abstract class RefasterTemplateData {
  public static RefasterTemplateData create(
      String name, String description, String link, SeverityLevel severityLevel) {
    return new AutoValue_RefasterTemplateData(name, description, link, severityLevel);
  }

  abstract String name();

  abstract String description();

  abstract String link();

  abstract SeverityLevel severityLevel();
}
