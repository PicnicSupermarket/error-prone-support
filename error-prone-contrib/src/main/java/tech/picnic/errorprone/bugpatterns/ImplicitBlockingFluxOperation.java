package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import tech.picnic.errorprone.bugpatterns.util.ThirdPartyLibrary;

@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "`Flux#toStream` and `Flux#toIterable` are documented to block, but this is not apparent from the method signature; please make sure that they are used with this in mind.",
    link = BUG_PATTERNS_BASE_URL + "ImplicitBlockingFluxOperation",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public class ImplicitBlockingFluxOperation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> FLUX =
      Suppliers.typeFromString("reactor.core.publisher.Flux");
  public static final MethodMatchers.MethodClassMatcher FLUX_METHOD =
      instanceMethod().onDescendantOf(FLUX);
  private static final Matcher<ExpressionTree> FLUX_TO_ITERABLE =
      FLUX_METHOD.named("toIterable").withNoParameters();
  private static final Matcher<ExpressionTree> FLUX_TO_STREAM =
      FLUX_METHOD.named("toStream").withNoParameters();
  private static final Matcher<ExpressionTree> FLUX_IMPLICIT_BLOCKING_METHOD =
      anyOf(FLUX_TO_ITERABLE, FLUX_TO_STREAM);

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_IMPLICIT_BLOCKING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    description.addFix(getSuppressWarningsFix(state));
    if (ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
      description.addFix(getGuavaFix(tree, state));
    }
    description.addFix(getUnmodifiableListFix(tree, state));

    return description.build();
  }

  private static SuggestedFix getSuppressWarningsFix(VisitorState state) {
    return SuggestedFixes.addSuppressWarnings(state, "ImplicitBlockingFluxOperation");
  }

  private static SuggestedFix getGuavaFix(MethodInvocationTree tree, VisitorState state) {
    SuggestedFix.Builder guavaFix = SuggestedFix.builder();
    String toImmutableList =
        SuggestedFixes.qualifyStaticImport(
            "com.google.common.collect.ImmutableList.toImmutableList", guavaFix, state);
    return getCollectAndBlockFix(tree, state, guavaFix, toImmutableList + "()").build();
  }

  private static SuggestedFix getUnmodifiableListFix(
      MethodInvocationTree tree, VisitorState state) {
    SuggestedFix.Builder unmodifiableListFix = SuggestedFix.builder();
    String toUnmodifiableList =
        SuggestedFixes.qualifyStaticImport(
            "java.util.stream.Collectors.toUnmodifiableList", unmodifiableListFix, state);
    return getCollectAndBlockFix(tree, state, unmodifiableListFix, toUnmodifiableList + "()")
        .build();
  }

  /**
   * @param collector expression
   * @return `collect(...).block()...` fix with specified collector and postfix to match the
   *     original expression tree.
   */
  private static SuggestedFix.Builder getCollectAndBlockFix(
      MethodInvocationTree tree, VisitorState state, SuggestedFix.Builder fix, String collector) {
    String postfix = getCollectAndBlockFixPostfix(tree, state);
    // XXX: replace DIY string replace fix with something more resilient
    String flux =
        state.getSourceForNode(tree).substring(0, state.getSourceForNode(tree).indexOf("."));
    String replacement = String.format("%s.collect(%s).block()%s", flux, collector, postfix);
    // fix.merge(SuggestedFix.replace(startPos, endPos, replacement));
    fix.merge(SuggestedFix.replace(tree, replacement));
    return fix;
  }

  /**
   * @return postfix for `Flux.collect(...).block()` fix to match the original expression tree.
   */
  private static String getCollectAndBlockFixPostfix(
      MethodInvocationTree tree, VisitorState state) {
    if (FLUX_TO_STREAM.matches(tree, state)) {
      return ".stream()";
    }
    return "";
  }
}
