package tech.picnic.errorprone.documentation.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/**
 * Object containing all data related to a Refaster template collection. This is solely used for
 * serialization.
 */
// XXX: This class is not yet used.
@AutoValue
@JsonDeserialize(as = AutoValue_RefasterTemplateCollectionData.class)
public abstract class RefasterTemplateCollectionData {
  static RefasterTemplateCollectionData create(
      String name, String description, String link, ImmutableList<RefasterTemplateData> templates) {
    return new AutoValue_RefasterTemplateCollectionData(name, description, link, templates);
  }

  abstract String name();

  abstract String description();

  abstract String link();

  abstract ImmutableList<RefasterTemplateData> templates();
}
