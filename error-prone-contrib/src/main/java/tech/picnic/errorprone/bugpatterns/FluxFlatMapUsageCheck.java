package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import java.util.function.Function;
import reactor.core.publisher.Flux;

/** A {@link BugChecker} which flags usages of {@link Flux#flatMap(Function)}s. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "FluxFlatMapUsage",
    summary =
        "`Flux#flatMap` is not allowed, please use `Flux#concatMap` or specify an argument for the concurrency.",
    explanation =
        "`Flux#flatMap` provides unbounded parallelism and is not guaranteed to be sequential. "
            + "Therefore, we disallow the use of the non-overloaded `Flux#flatMap`.",
    linkType = LinkType.NONE,
    severity = SeverityLevel.ERROR,
    tags = StandardTags.LIKELY_ERROR)
public final class FluxFlatMapUsageCheck extends BugChecker implements MemberSelectTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String NAME_CONCURRENCY_ARGUMENT = "MAX_CONCURRENCY";
  private static final Matcher<ExpressionTree> FLUX_FLATMAP =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Flux")
          .named("flatMap")
          .withParameters(Function.class.getName());

  @Override
  public Description matchMemberSelect(MemberSelectTree tree, VisitorState state) {
    if (!FLUX_FLATMAP.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Tree parentExpression = state.getPath().getParentPath().getLeaf();

    return buildDescription(tree)
        .setMessage(message())
        .addFix(
            SuggestedFix.builder()
                .replace(tree, Util.treeToString(tree, state).replace("flatMap", "concatMap"))
                .build())
        .addFix(
            SuggestedFix.builder()
                .replace(
                    parentExpression,
                    getReplacementWithConcurrencyArgument(parentExpression, state))
                .build())
        .build();
  }

  private static String getReplacementWithConcurrencyArgument(
      Tree parentExpression, VisitorState state) {
    String parentString = Util.treeToString(parentExpression, state);
    return String.format(
        "%s, %s)",
        parentString.substring(0, parentString.lastIndexOf(')')), NAME_CONCURRENCY_ARGUMENT);
  }
}
