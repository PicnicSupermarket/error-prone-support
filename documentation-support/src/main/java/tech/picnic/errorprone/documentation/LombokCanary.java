package tech.picnic.errorprone.documentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

// XXX: Rename all this stuff.
@Data
@SuppressWarnings("EqualsMissingNullable") // XXX: Drop after EP upgrade.
final class LombokCanary {
  @JsonProperty("custom_field_name")
  private @Nullable String field;
}
