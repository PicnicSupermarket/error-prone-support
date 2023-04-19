package tech.picnic.errorprone.openai;

import static java.util.stream.Collectors.joining;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An {@link IssueExtractor} that recognizes compiler messages as formatted by Maven's Plexus
 * Compiler module.
 *
 * @see <a
 *     href="https://github.com/codehaus-plexus/plexus-compiler/blob/a5775b2258349b7c0d7c7759f162c80672328a0e/plexus-compiler-api/src/main/java/org/codehaus/plexus/compiler/CompilerMessage.java#L271-L296">Plexus
 *     Compiler message format</a>
 */
// XXX: Can path be relative? If so, that'd clash with `CheckStyleIssueExtractor`.
// ^ I think we should allow this, and then introduce a way to _chain_ extractors using short
// circuiting.
// XXX: Also replace "Did you mean to remove" with "Remove"?
// XXX: Also replace "Did you mean '" with "Instead use '"?
// XXX: Concat "Did you mean" message to the preceding line?
// XXX: More generally join lines by `. ` (inserting dot if necessary)?
// ^ For debuggability it could make sense to keep the original message, and _separately_ generate
// the OpenAI prompt.
final class PlexusCompilerIssueExtractor implements IssueExtractor<String> {
  // XXX: Extend regex to match the following:
  // https://github.com/codehaus-plexus/plexus-compiler/blob/a5775b2258349b7c0d7c7759f162c80672328a0e/plexus-compiler-api/src/main/java/org/codehaus/plexus/compiler/CompilerMessage.java#L271-L296.
  private static final Pattern LOG_LINE_FORMAT =
      Pattern.compile(
          "^(?<file>/.+?\\.java):(?:\\[(?<line>\\d+)(?:,(?<column>\\d+))?\\])? (?<message>.+)$",
          Pattern.DOTALL);
  private static final Pattern ERROR_PRONE_DOCUMENTATION_REFERENCE =
      Pattern.compile("^\\s*\\(see .+\\)\\s*$");

  private final IssueExtractor<String> delegate = new RegexIssueExtractor(LOG_LINE_FORMAT);

  @Override
  public Stream<Issue<String>> extract(String str) {
    return delegate
        .extract(str)
        .map(issue -> issue.withMessage(removeErrorProneDocumentationReference(issue.message())));
  }

  private static String removeErrorProneDocumentationReference(String message) {
    return message
        .lines()
        .filter(line -> !ERROR_PRONE_DOCUMENTATION_REFERENCE.matcher(line).matches())
        .collect(joining("\n"));
  }
}
