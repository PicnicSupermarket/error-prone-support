package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.stream.Stream;

// XXX: Document
interface IssueExtractor {
  Stream<Issue> extract(String str);

  // XXX: Make `Path` a `String` and do path lookup post collection? (This would simplify things,
  // but may close off some future possibilities.)
  // ^ Not really. Where it matters we can double-resolve.
  // XXX: ^ Also simplifies testing.
  // XXX: Or move to separate file?
  record Issue(String message, String file, int line, OptionalInt column) {

    Issue withMessage(String message) {
      return new Issue(message, file, line, column);
    }
  }
}
