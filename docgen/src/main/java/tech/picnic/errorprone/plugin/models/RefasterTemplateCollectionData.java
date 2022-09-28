package tech.picnic.errorprone.plugin.models;

import com.google.auto.value.AutoValue;
import java.util.List;

/**
 * Object containing all data related to a Refaster template collection. This is solely used for
 * serialization.
 */
@AutoValue
public abstract class RefasterTemplateCollectionData {
  public static RefasterTemplateCollectionData create(
      String name, String description, String link, List<RefasterTemplateData> templates) {
    return new AutoValue_RefasterTemplateCollectionData(name, description, link, templates);
  }

  abstract String name();

  abstract String description();

  abstract String link();

  abstract List<RefasterTemplateData> templates();
}
