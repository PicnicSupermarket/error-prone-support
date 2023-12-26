package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.suppliers.Suppliers.OBJECT_TYPE;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
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
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ASTHelpers.TargetType;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import java.util.Arrays;
import java.util.List;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that flags redundant identity conversions. */
// XXX: Consider detecting cases where a flagged expression is passed to a method, and where removal
// of the identity conversion would cause a different method overload to be selected. Depending on
// the target method such a modification may change the code's semantics or performance.
// XXX: Also flag `Stream#map`, `Mono#map` and `Flux#map` invocations where the given transformation
// is effectively the identity operation.
// XXX: Also flag nullary instance method invocations that represent an identity conversion, such as
// `Boolean#booleanValue()`, `Byte#byteValue()` and friends.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid or clarify identity conversions",
    link = BUG_PATTERNS_BASE_URL + "IdentityConversion",
    linkType = CUSTOM,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class IdentityConversion extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> IS_CONVERSION_METHOD =
      anyOf(
          staticMethod()
              .onClassAny(
                  Primitives.allWrapperTypes().stream()
                      .map(Class::getName)
                      .collect(toImmutableSet()))
              .named("valueOf"),
          staticMethod().onClass(String.class.getCanonicalName()).named("valueOf"),
          staticMethod()
              .onClassAny(
                  ImmutableBiMap.class.getCanonicalName(),
                  ImmutableList.class.getCanonicalName(),
                  ImmutableListMultimap.class.getCanonicalName(),
                  ImmutableMap.class.getCanonicalName(),
                  ImmutableMultimap.class.getCanonicalName(),
                  ImmutableMultiset.class.getCanonicalName(),
                  ImmutableRangeMap.class.getCanonicalName(),
                  ImmutableRangeSet.class.getCanonicalName(),
                  ImmutableSet.class.getCanonicalName(),
                  ImmutableSetMultimap.class.getCanonicalName(),
                  ImmutableTable.class.getCanonicalName())
              .named("copyOf"),
          staticMethod().onClass(Matchers.class.getCanonicalName()).namedAnyOf("allOf", "anyOf"),
          staticMethod().onClass("reactor.adapter.rxjava.RxJava2Adapter"),
          staticMethod()
              .onClass("reactor.core.publisher.Flux")
              .namedAnyOf("concat", "firstWithSignal", "from", "merge"),
          staticMethod().onClass("reactor.core.publisher.Mono").namedAnyOf("from", "fromDirect"));

  /** Instantiates a new {@link IdentityConversion} instance. */
  public IdentityConversion() {}

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

    if (sourceType.isPrimitive()
        && state.getPath().getParentPath().getLeaf() instanceof MemberSelectTree) {
      /*
       * The result of the conversion method is dereferenced, while the source type is a primitive:
       * dropping the conversion would yield uncompilable code.
       */
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "This method invocation appears redundant; remove it or suppress this warning and "
                + "add a comment explaining its purpose")
        .addFix(SuggestedFix.replace(tree, SourceCode.treeToString(sourceTree, state)))
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
