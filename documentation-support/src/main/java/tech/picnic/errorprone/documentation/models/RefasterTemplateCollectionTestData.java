package tech.picnic.errorprone.documentation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

// XXX: Make properties package-private if type is moved to shared package.
@AutoValue
@JsonDeserialize(as = AutoValue_RefasterTemplateCollectionTestData.class)
public abstract class RefasterTemplateCollectionTestData {
  public static RefasterTemplateCollectionTestData create(
      String templateCollection,
      boolean isInput,
      ImmutableList<RefasterTemplateTestData> templatesTests) {
    return new AutoValue_RefasterTemplateCollectionTestData(
        templateCollection, isInput, templatesTests);
  }

  public abstract String templateCollection();

  // XXX: This annotation prevents serialization of fields `isInput` *and* `input`. Review.
  @JsonProperty("isInput")
  public abstract boolean isInput();

  public abstract ImmutableList<RefasterTemplateTestData> templateTests();
}
