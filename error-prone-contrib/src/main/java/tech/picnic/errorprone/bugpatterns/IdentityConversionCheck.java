package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ASTHelpers.TargetType;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.List;

/** A {@link BugChecker} that flags redundant identity conversions. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "IdentityConversion",
    summary = "Avoid or clarify identity conversions",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.SIMPLIFICATION)
public final class IdentityConversionCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> IS_CONVERSION_METHOD =
      anyOf(
          staticMethod()
              .onClassAny(
                  "com.google.common.collect.ImmutableBiMap",
                  "com.google.common.collect.ImmutableList",
                  "com.google.common.collect.ImmutableListMultimap",
                  "com.google.common.collect.ImmutableMap",
                  "com.google.common.collect.ImmutableMultimap",
                  "com.google.common.collect.ImmutableMultiset",
                  "com.google.common.collect.ImmutableRangeMap",
                  "com.google.common.collect.ImmutableRangeSet",
                  "com.google.common.collect.ImmutableSet",
                  "com.google.common.collect.ImmutableSetMultimap",
                  "com.google.common.collect.ImmutableSortedMap",
                  "com.google.common.collect.ImmutableSortedMultiset",
                  "com.google.common.collect.ImmutableSortedSet",
                  "com.google.common.collect.ImmutableTable")
              .named("copyOf"),
          staticMethod()
              .onClassAny(
                  Byte.class.getName(),
                  Character.class.getName(),
                  Double.class.getName(),
                  Float.class.getName(),
                  Integer.class.getName(),
                  String.class.getName())
              .named("valueOf"),
          staticMethod().onClass("reactor.adapter.rxjava.RxJava2Adapter"),
          staticMethod()
              .onClass("reactor.core.publisher.Flux")
              .namedAnyOf("concat", "firstWithSignal", "from", "merge"),
          staticMethod().onClass("reactor.core.publisher.Mono").namedAnyOf("from", "fromDirect"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.size() != 1 || !IS_CONVERSION_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree sourceTree = arguments.get(0);
    Type sourceType = ASTHelpers.getType(sourceTree);
    TargetType targetType = ASTHelpers.targetType(state);
    if (sourceType == null
        || targetType == null
        || !state.getTypes().isSubtype(sourceType, targetType.type())) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "This method invocation appears redundant; remove it or suppress this warning and "
                + "add an comment explaining its purpose")
        .addFix(SuggestedFix.replace(tree, state.getSourceForNode(sourceTree)))
        .addFix(SuggestedFixes.addSuppressWarnings(state, canonicalName()))
        .build();
  }
}
