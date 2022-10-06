package tech.picnic.errorprone.plugin.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RefasterTemplateTestData {
  public static RefasterTemplateTestData create(
      String templateCollection, String templateName, String templateTestContent) {
    return new AutoValue_RefasterTemplateTestData(
        templateCollection, templateName, templateTestContent);
  }

  @JsonProperty
  abstract String templateCollection();

  @JsonProperty
  abstract String templateName();

  @JsonProperty
  abstract String templateTestContent();
}
