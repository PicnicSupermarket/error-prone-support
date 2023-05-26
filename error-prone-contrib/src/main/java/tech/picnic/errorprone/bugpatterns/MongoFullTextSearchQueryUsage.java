package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

/**
 * A {@link BugChecker} that flags usages of Mongo $text filters used for full text searches.
 *
 * @see <a href="https://www.mongodb.com/docs/manual/text-search/">Mongo Text Search</a>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Usage of Mongo for full-text search queries via the `$text` operator is discouraged.",
    link = BUG_PATTERNS_BASE_URL + "MongoFullTextSearchQueryUsage",
    linkType = CUSTOM,
    severity = WARNING,
    tags = PERFORMANCE)
public final class MongoFullTextSearchQueryUsage extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> TEXT_FILTER_INVOCATION =
      staticMethod().onClass("com.mongodb.client.model.Filters").named("text");

  /** Instantiates a new {@link MongoFullTextSearchQueryUsage} instance. */
  public MongoFullTextSearchQueryUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!TEXT_FILTER_INVOCATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree);
  }
}
