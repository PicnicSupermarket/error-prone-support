package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyMethod;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.toType;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.stream.Stream;

/** Bla. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "XXX: Write this.",
    link = BUG_PATTERNS_BASE_URL + "MonoZipOfMonoVoidUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MonoZipOfMonoVoidUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Supplier<Type> EMPTY_MONO =
      Suppliers.typeFromString("reactor.core.publisher.MonoEmpty");
  private static final Supplier<Type> MONO_VOID =
      VisitorState.memoize(generic(type("reactor.core.publisher.Mono"), type("java.lang.Void")));

  // On Mono.zip, at least one element should match empty in order to proceed.
  private static final Matcher<ExpressionTree> MONO_ZIP_AND_WITH =
      anyOf(
          anyMethod().onClass("reactor.core.publisher.Mono").namedAnyOf("zip", "zipWith"),
          toType(MethodInvocationTree.class, hasArgumentOfType(MONO_VOID)));
  // On mono.zipWith, argument should match empty in order to proceed.
  private static final Matcher<ExpressionTree> DYNAMIC_MONO_ZIP =
      allOf(
          instanceMethod().onDescendantOf(MONO).named("zipWith"),
          toType(MethodInvocationTree.class, hasArgumentOfType(EMPTY_MONO)));

  // On emptyMono.zipWith, argument should match non-empty in order to proceed.
  private static final Matcher<ExpressionTree> DYNAMIC_EMPTY_MONO_WITH_NON_EMPTY_PARAM_ZIP =
      allOf(
          instanceMethod().onDescendantOf(EMPTY_MONO).named("zipWith"),
          toType(MethodInvocationTree.class, hasArgumentOfType(MONO)));

  // On emptyMono.zipWith, argument should match empty in order to proceed.
  private static final Matcher<ExpressionTree> DYNAMIC_EMPTY_MONO_WITH_EMPTY_PARAM_ZIP =
      allOf(
          instanceMethod().onDescendantOf(EMPTY_MONO).named("zipWith"),
          toType(MethodInvocationTree.class, hasArgumentOfType(EMPTY_MONO)));

  //  private static final Matcher<ExpressionTree> MONO_ZIP =
  //      anyOf(
  //          DYNAMIC_MONO_ZIP,
  //          DYNAMIC_EMPTY_MONO_WITH_NON_EMPTY_PARAM_ZIP,
  //          DYNAMIC_EMPTY_MONO_WITH_EMPTY_PARAM_ZIP,
  //              MONO_ZIP_AND_WITH);

  /** Instantiates a new {@link MonoZipOfMonoVoidUsage} instance. */
  public MonoZipOfMonoVoidUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MONO_ZIP_AND_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    // ASTHelpers.getType(tree.getArguments().get(0))
    //    MoreTypes.generic(MoreTypes.type("reactor.core.publisher.Mono"),
    // MoreTypes.type("java.lang.Void")).get(state)
    if (MONO_ZIP_AND_WITH.matches(tree, state)) {
      ImmutableList<? extends ExpressionTree> arguments = ImmutableList.copyOf(tree.getArguments());

      String replacement =
          Streams.concat(
                  Stream.of(
                      arguments.stream().findFirst().map(ExpressionTree::toString).orElseThrow()),
                  arguments.stream()
                      .skip(1)
                      .map(ExpressionTree::toString)
                      .map(arg -> String.format("(%s)", arg)))
              .collect(joining(".then"));
      SuggestedFix staticZipUsage = SuggestedFix.replace(tree, replacement);
      description.addFix(staticZipUsage);
    } else {
      SuggestedFix instanceZipUsage =
          SuggestedFixes.renameMethodInvocation(tree, "concatWith", state);
      description.addFix(instanceZipUsage);
    }

    return description.build();
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    if (!MONO_ZIP_AND_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    // Method references are expected to occur very infrequently; generating both variants of
    // suggested fixes is not worth the trouble.
    return describeMatch(tree);
  }

  private static Matcher<MethodInvocationTree> hasArgumentOfType(Supplier<Type> type) {
    return (tree, state) ->
        tree.getArguments().stream()
            .anyMatch(arg -> ASTHelpers.isSubtype(ASTHelpers.getType(arg), type.get(state), state));
  }
}
