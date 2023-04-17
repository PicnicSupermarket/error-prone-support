package tech.picnic.errorprone.openai;

import static java.util.stream.Collectors.joining;

import java.util.regex.Pattern;
import java.util.stream.Stream;

// [WARNING]
// /home/sschroevers/workspace/picnic/error-prone-support/openai-coder/src/main/java/tech/picnic/errorprone/openai/AiPatcher.java:[30,22] no comment
// [WARNING]
// /home/sschroevers/workspace/picnic/error-prone-support/openai-coder/src/main/java/tech/picnic/errorprone/openai/AiPatcher.java:[22,32] [UnusedVariable] The field 'FILE_LOCATION_MARKER' is never read.
//     (see https://errorprone.info/bugpattern/UnusedVariable)
//   Did you mean to remove this line or 'static {
// Pattern.compile("^(.*?\\.java):\\[(\\d+)(?:,(\\d+))?\\] "); }'?

// XXX: Create another `LogLineAnalyzer` for Error Prone test compiler output.
// XXX: Also replace "Did you mean to remove" with "Remove"?
// XXX: Also replace "Did you mean '" with "Instead use '"?
// ^ For debuggability it could make sense to keep the original message, and _separately_ generate
// the OpenAI prompt.
final class JavacIssueExtractor implements IssueExtractor<String> {
  private static final Pattern LOG_LINE_FORMAT =
      Pattern.compile(
          "^(?<file>/.+?\\.java):\\[(?<line>\\d+)(?:,(?<column>\\d+))?\\] (?<message>.+)$",
          Pattern.DOTALL);
  private static final Pattern ERROR_PRONE_DOCUMENTATION_REFERENCE =
      Pattern.compile("^\\s*\\(see .+\\)\\s+$");

  private final IssueExtractor<String> delegate = new RegexIssueExtractor(LOG_LINE_FORMAT);

  @Override
  public Stream<Issue<String>> extract(String str) {
    return delegate
        .extract(str)
        .map(issue -> issue.withMessage(removeErrorProneDocumentationReference(issue.message())));
  }

  // XXX: This doesn't work.
  private static String removeErrorProneDocumentationReference(String message) {
    return message
        .lines()
        .filter(line -> !ERROR_PRONE_DOCUMENTATION_REFERENCE.matcher(line).matches())
        .collect(joining(""));
  }

  //  // XXX: drop the `(see` line.
  //  @Override
  //  public Stream<Issue> extract(String str) {
  //    Matcher matcher = FILE_LOCATION_MARKER.matcher(str);
  //    if (!matcher.find()) {
  //      return Stream.empty();
  //    }
  //
  //    return pathFinder
  //        .findPath(matcher.group("file"))
  //        .map(
  //            p ->
  //                new Issue(
  //                    str.substring(matcher.end()),
  //                    p,
  //                    Integer.parseInt(matcher.group("line")),
  //                    matcher.group("column") == null
  //                        ? OptionalInt.empty()
  //                        : OptionalInt.of(Integer.parseInt(matcher.group("column")))))
  //        .stream();
  //  }
}
