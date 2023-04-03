package tech.picnic.errorprone.documentation.models;

import com.google.auto.value.AutoValue;
import tech.picnic.errorprone.documentation.models.AutoValue_RefasterTemplateTestData;

@AutoValue
public abstract class RefasterTemplateTestData {
  public static RefasterTemplateTestData create(String templateName, String templateTestContent) {
    return new AutoValue_RefasterTemplateTestData(templateName, templateTestContent);
  }

  abstract String templateName();

  abstract String templateTestContent();
}
