package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
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
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;
import tech.picnic.errorprone.bugpatterns.util.ThirdPartyLibrary;

/**
 * A {@link BugChecker} that flags usages of {@link reactor.core.publisher.Flux} methods that are
 * documented to block, but that behaviour is not apparent from their signature.
 *
 * <p>The alternatives suggested are explicitly blocking operators, highlighting actual behavior,
 * not adequate for automatic replacements, they need manual review.
 */
@AutoService(BugChecker.class)
@BugPattern(
    explanation =
        "`Flux#toStream` and `Flux#toIterable` are documented to block, "
            + "but this is not apparent from the method signature; "
            + "please make sure that they are used with this in mind",
    summary = "Accidental blocking of `Flux` with convenience method",
    link = BUG_PATTERNS_BASE_URL + "ImplicitBlockingFluxOperation",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class ImplicitBlockingFluxOperation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLUX_WITH_IMPLICIT_BLOCK =
      instanceMethod()
          .onDescendantOf(Suppliers.typeFromString("reactor.core.publisher.Flux"))
          .namedAnyOf("toIterable", "toStream")
          .withNoParameters();

  /** Instantiates a new {@link ImplicitBlockingFluxOperation} instance. */
  public ImplicitBlockingFluxOperation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_WITH_IMPLICIT_BLOCK.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    description.addFix(SuggestedFixes.addSuppressWarnings(state, "ImplicitBlockingFluxOperation"));
    if (ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
      description.addFix(getGuavaFix(tree, state));
    }
    description.addFix(getUnmodifiableListFix(tree, state));

    return description.build();
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
   * Merges `flux.collect(...).block()...` fix into given fix with specified collector and postfix
   * to match the original expression tree.
   *
   * @param collector expression.
   * @return `flux.collect(...).block()...` fix with specified collector and postfix to match the
   *     original expression tree.
   */
  private static SuggestedFix.Builder getCollectAndBlockFix(
      MethodInvocationTree tree, VisitorState state, SuggestedFix.Builder fix, String collector) {
    String postfix = getCollectAndBlockFixPostfix(tree, state);

    // XXX: replace DIY string replace fix with something more resilient
    String source = state.getSourceForNode(tree);
    String flux = source.substring(0, source.indexOf("."));
    String replacement = String.format("%s.collect(%s).block()%s", flux, collector, postfix);
    fix.merge(SuggestedFix.replace(tree, replacement));
    return fix;
  }

  /**
   * Finds the extension of `Flux.collect(...).block()` expression to match the original expression
   * tree.
   */
  private static String getCollectAndBlockFixPostfix(
      MethodInvocationTree tree, VisitorState state) {
    return SourceCode.treeToString(tree.getMethodSelect(), state).endsWith("toIterable")
        ? ""
        : ".stream()";
  }
}
