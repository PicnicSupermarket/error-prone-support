package tech.picnic.errorprone.plugin.models;

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
  abstract boolean isInput();

  @JsonProperty
  abstract List<RefasterTemplateTestData> templateTests();

}
