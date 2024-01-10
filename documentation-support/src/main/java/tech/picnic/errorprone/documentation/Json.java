package tech.picnic.errorprone.documentation;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.errorprone.annotations.FormatMethod;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Utility class that offers mutually consistent JSON serialization and deserialization operations,
 * without further specifying the exact schema used.
 */
final class Json {
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
          .registerModules(new GuavaModule(), new ParameterNamesModule());

  private Json() {}

  static <T> T read(Path path, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(path.toFile(), clazz);
    } catch (IOException e) {
      throw failure(e, "Failure reading from '%s'", path);
    }
  }

  static <T> void write(Path path, T object) {
    try {
      OBJECT_MAPPER.writeValue(path.toFile(), object);
    } catch (IOException e) {
      throw failure(e, "Failure writing to '%s'", path);
    }
  }

  @FormatMethod
  private static UncheckedIOException failure(IOException cause, String format, Object... args) {
    return new UncheckedIOException(String.format(format, args), cause);
  }
}
