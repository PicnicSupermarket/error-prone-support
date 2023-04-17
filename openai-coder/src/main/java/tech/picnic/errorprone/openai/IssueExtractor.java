package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.stream.Stream;

// XXX: Document
interface IssueExtractor<F> {
  Stream<Issue<F>> extract(String str);

  // XXX: Move to separate file?
  // XXX: `file` as first argument? (If so, also swap method order.)
  record Issue<F>(String message, F file, int line, OptionalInt column) {
    Issue<F> withMessage(String message) {
      return new Issue<>(message, file, line, column);
    }

    <T> Issue<T> withFile(T file) {
      return new Issue<>(message, file, line, column);
    }

    String description() {
      return column().isEmpty()
          ? String.format("Line %s: %s", line(), message())
          : String.format("Line %s, column %s: %s", line(), column().getAsInt(), message());
    }
  }
}
