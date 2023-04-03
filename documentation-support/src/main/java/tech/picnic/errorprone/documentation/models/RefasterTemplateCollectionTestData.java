package tech.picnic.errorprone.documentation.models;

import com.google.auto.value.AutoValue;
import java.util.List;
import tech.picnic.errorprone.documentation.models.AutoValue_RefasterTemplateCollectionTestData;

@AutoValue
public abstract class RefasterTemplateCollectionTestData {
  public static RefasterTemplateCollectionTestData create(
      String templateCollection, boolean isInput, List<RefasterTemplateTestData> templatesTests) {
    return new AutoValue_RefasterTemplateCollectionTestData(
        templateCollection, isInput, templatesTests);
  }

  abstract String templateCollection();

  abstract boolean isInput();

  abstract List<RefasterTemplateTestData> templateTests();
}
