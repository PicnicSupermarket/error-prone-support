package tech.picnic.errorprone.documentation.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

// XXX: Make properties package-private if type is moved to shared package.
@AutoValue
@JsonDeserialize(as = AutoValue_RefasterTemplateTestData.class)
public abstract class RefasterTemplateTestData {
  public static RefasterTemplateTestData create(String templateName, String templateTestContent) {
    return new AutoValue_RefasterTemplateTestData(templateName, templateTestContent);
  }

  public abstract String templateName();

  public abstract String templateTestContent();
}
