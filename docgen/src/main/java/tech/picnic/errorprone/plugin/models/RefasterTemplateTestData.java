package tech.picnic.errorprone.plugin.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RefasterTemplateTestData {
  public static RefasterTemplateTestData create(String templateName, String templateTestContent) {
    return new AutoValue_RefasterTemplateTestData(templateName, templateTestContent);
  }

  @JsonProperty
  abstract String templateName();

  @JsonProperty
  abstract String templateTestContent();
}
