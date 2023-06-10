package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
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
 * A {@link BugChecker} that flags usages of Mongo {@code $text} filters used for full text
 * searches.
 *
 * @see <a href="https://www.mongodb.com/docs/manual/text-search/">Mongo Text Search</a>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid the `$text` operator in Mongo's full-text search queries",
    link = BUG_PATTERNS_BASE_URL + "MongoFullTextSearchQueryUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = PERFORMANCE)
public final class MongoFullTextSearchQueryUsage extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> MONGO_FILTERS_TEXT_METHOD =
      staticMethod().onClass("com.mongodb.client.model.Filters").named("text");

  /** Instantiates a new {@link MongoFullTextSearchQueryUsage} instance. */
  public MongoFullTextSearchQueryUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return MONGO_FILTERS_TEXT_METHOD.matches(tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }
}
