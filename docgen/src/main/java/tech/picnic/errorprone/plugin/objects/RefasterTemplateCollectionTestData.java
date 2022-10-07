package tech.picnic.errorprone.plugin.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class RefasterTemplateCollectionTestData {
  public static RefasterTemplateCollectionTestData create(
      String templateCollection, List<RefasterTemplateTestData> templatesTests, boolean isInput) {
    return new AutoValue_RefasterTemplateCollectionTestData(
        templateCollection, templatesTests, isInput);
  }

  @JsonProperty
  abstract String templateCollection();

  @JsonProperty
  abstract List<RefasterTemplateTestData> templateTests();

  @JsonProperty
  abstract boolean isInput();
}
