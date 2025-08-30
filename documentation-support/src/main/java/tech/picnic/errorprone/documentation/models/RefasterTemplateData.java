package tech.picnic.errorprone.documentation.models;

import com.google.errorprone.BugPattern.SeverityLevel;

// XXX: This class is not yet used.
record RefasterTemplateData(
    String name, String description, String link, SeverityLevel severityLevel) {
  static RefasterTemplateData create(
      String name, String description, String link, SeverityLevel severityLevel) {
    return new RefasterTemplateData(name, description, link, severityLevel);
  }
}
