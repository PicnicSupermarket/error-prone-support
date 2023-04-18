package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Interface of types that implement a procedure to extract {@link Issue}s from a given string.
 *
 * @param <F> The type used to describe the location of files against which an issue is reported.
 */
@FunctionalInterface
interface IssueExtractor<F> {
  /** Extracts zero or more {@link Issue}s from the given string. */
  Stream<Issue<F>> extract(String str);

  // XXX: Move to separate file?
  record Issue<F>(F file, OptionalInt line, OptionalInt column, String message) {
    <T> Issue<T> withFile(T file) {
      return new Issue<>(file, line, column, message);
    }

    Issue<F> withMessage(String message) {
      return new Issue<>(file, line, column, message);
    }

    String description() {
      return line().isEmpty()
          ? message()
          : column().isEmpty()
              ? String.format("Line %s: %s", line().getAsInt(), message())
              : String.format(
                  "Line %s, column %s: %s", line().getAsInt(), column().getAsInt(), message());
    }
  }
}
