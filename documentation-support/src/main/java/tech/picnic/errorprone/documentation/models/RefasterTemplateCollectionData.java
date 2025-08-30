package tech.picnic.errorprone.documentation.models;

import com.google.common.collect.ImmutableList;

/**
 * Object containing all data related to a Refaster template collection. This is solely used for
 * serialization.
 */
// XXX: This class is not yet used.
record RefasterTemplateCollectionData(
    String name, String description, String link, ImmutableList<RefasterTemplateData> templates) {
  static RefasterTemplateCollectionData create(
      String name, String description, String link, ImmutableList<RefasterTemplateData> templates) {
    return new RefasterTemplateCollectionData(name, description, link, templates);
  }
}
