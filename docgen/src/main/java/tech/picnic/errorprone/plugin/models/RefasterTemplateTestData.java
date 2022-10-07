package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RefasterTemplateTestData {
  public static RefasterTemplateTestData create(String templateName, String templateTestContent) {
    return new AutoValue_RefasterTemplateTestData(templateName, templateTestContent);
  }

  abstract String templateName();

  abstract String templateTestContent();
}
