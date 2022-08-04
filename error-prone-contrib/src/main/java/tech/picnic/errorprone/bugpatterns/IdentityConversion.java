package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.suppliers.Suppliers.OBJECT_TYPE;

import com.google.auto.service.AutoService;
import com.google.common.primitives.Primitives;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.TypesWithUndefinedEquality;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ASTHelpers.TargetType;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import java.util.Arrays;
import java.util.List;
import tech.picnic.errorprone.bugpatterns.util.Util;

/** A {@link BugChecker} that flags redundant identity conversions. */
// XXX: Consider detecting cases where a flagged expression is passed to a method, and where removal
// of the identify conversion would cause a different method overload to be selected. Depending on
// the target method such a modification may change the code's semantics or performance.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid or clarify identity conversions",
    linkType = NONE,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class IdentityConversion extends BugChecker implements MethodInvocationTreeMatcher {
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
                  "com.google.common.collect.ImmutableTable")
              .named("copyOf"),
          staticMethod()
              .onClassAny(
                  Primitives.allWrapperTypes().stream()
                      .map(Class::getName)
                      .collect(toImmutableSet()))
              .named("valueOf"),
          staticMethod().onClass(String.class.getName()).named("valueOf"),
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
    Type resultType = ASTHelpers.getType(tree);
    TargetType targetType = ASTHelpers.targetType(state);
    if (sourceType == null || resultType == null || targetType == null) {
      return Description.NO_MATCH;
    }

    if (!state.getTypes().isSameType(sourceType, resultType)
        && !isConvertibleWithWellDefinedEquality(sourceType, targetType.type(), state)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "This method invocation appears redundant; remove it or suppress this warning and "
                + "add a comment explaining its purpose")
        .addFix(SuggestedFix.replace(tree, Util.treeToString(sourceTree, state)))
        .addFix(SuggestedFixes.addSuppressWarnings(state, canonicalName()))
        .build();
  }

  private static boolean isConvertibleWithWellDefinedEquality(
      Type sourceType, Type targetType, VisitorState state) {
    Types types = state.getTypes();
    return !types.isSameType(targetType, OBJECT_TYPE.get(state))
        && types.isConvertible(sourceType, targetType)
        && Arrays.stream(TypesWithUndefinedEquality.values())
            .noneMatch(b -> b.matchesType(sourceType, state) || b.matchesType(targetType, state));
  }
}
