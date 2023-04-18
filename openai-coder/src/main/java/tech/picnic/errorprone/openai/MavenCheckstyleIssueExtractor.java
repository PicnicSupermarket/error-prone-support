package tech.picnic.errorprone.openai;

import java.util.regex.Pattern;
import java.util.stream.Stream;

// XXX: Add test cases.
// [ERROR] src/main/java/tech/picnic/errorprone/refaster/plugin/RefasterRuleCompiler.java:[13,1]
// (annotation) AnnotationUseStyle: Annotation style must be 'COMPACT_NO_ARRAY'.
// [ERROR] src/main/java/tech/picnic/errorprone/refaster/plugin/RefasterRuleCompiler.java:[14,15]
// (annotation) AnnotationUseStyle: Annotation array values cannot contain trailing comma.
// [ERROR] src/main/java/tech/picnic/errorprone/refaster/plugin/RefasterRuleCompiler.java:[16]
// (regexp) RegexpMultiline: Avoid blank lines at the start of a block.

/**
 * An {@link IssueExtractor} that recognizes Checkstyle violations as reported by the Maven
 * CheckStyle Plugin
 *
 * @see <a
 *     href="https://github.com/apache/maven-checkstyle-plugin/blob/997f1e4148ae6c0b399ed2a306a5cc8b365083b8/src/main/java/org/apache/maven/plugins/checkstyle/CheckstyleViolationCheckMojo.java#L728-L735">Maven
 *     CheckStyle Plugin message format</a>
 */
final class MavenCheckstyleIssueExtractor implements IssueExtractor<String> {
  private static final Pattern LOG_LINE_FORMAT =
      Pattern.compile(
          "^(?<file>.+?\\.java):\\[(?<line>\\d+)(?:,(?<column>\\d+))?\\] \\(.+?\\) .+?: (?<message>.+)$",
          Pattern.DOTALL);

  private final IssueExtractor<String> delegate = new RegexIssueExtractor(LOG_LINE_FORMAT);

  @Override
  public Stream<Issue<String>> extract(String str) {
    return delegate.extract(str);
  }
}
