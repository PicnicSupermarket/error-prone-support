package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;

/** TODO: Write Javadoc. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "You probably meant .filter.when(), right ?",
    link = BUG_PATTERNS_BASE_URL + "MonoFilterThen",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class MonoFilterThen extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Matcher<ExpressionTree> MONO_THEN =
      instanceMethod().onDescendantOf(MONO).named("then");

  /** Instantiates a new {@link MonoFilterThen} instance. */
  public MonoFilterThen() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (MONO_THEN.matches(tree, state)
        && ASTHelpers.getSymbol(ASTHelpers.getReceiver(tree))
            .getSimpleName()
            .toString()
            .equals("filter")) {
      return describeMatch(tree);
    }

    return Description.NO_MATCH;
  }
}
