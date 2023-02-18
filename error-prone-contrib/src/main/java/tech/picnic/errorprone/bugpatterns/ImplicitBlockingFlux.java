package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
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
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Position;
import java.util.stream.Stream;
import tech.picnic.errorprone.bugpatterns.util.ThirdPartyLibrary;

/**
 * A {@link BugChecker} that flags {@link reactor.core.publisher.Flux} operators that are implicitly
 * blocking.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid using `Flux` operators that implicitly block",
    link = BUG_PATTERNS_BASE_URL + "ImplicitBlockingFlux",
    linkType = CUSTOM,
    severity = WARNING,
    tags = {CONCURRENCY, PERFORMANCE})
public final class ImplicitBlockingFlux extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLUX_WITH_IMPLICIT_BLOCK =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Flux")
          .namedAnyOf("toIterable", "toStream")
          .withNoParameters();
  private static final Supplier<Type> STREAM = Suppliers.typeFromString(Stream.class.getName());

  /** Instantiates a new {@link ImplicitBlockingFlux} instance. */
  public ImplicitBlockingFlux() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_WITH_IMPLICIT_BLOCK.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Description.Builder description =
        buildDescription(tree).addFix(SuggestedFixes.addSuppressWarnings(state, canonicalName()));
    if (ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
      description.addFix(
          suggestBlockingElementCollection(
              tree, "com.google.common.collect.ImmutableList.toImmutableList", state));
    }
    description.addFix(
        suggestBlockingElementCollection(tree, "java.util.stream.Collectors.toList", state));

    return description.build();
  }

  private static SuggestedFix suggestBlockingElementCollection(
      MethodInvocationTree tree, String fullyQualifiedCollectorMethod, VisitorState state) {
    SuggestedFix.Builder importSuggestion = SuggestedFix.builder();
    String replacementMethodInvocation =
        SuggestedFixes.qualifyStaticImport(fullyQualifiedCollectorMethod, importSuggestion, state);

    boolean isStream =
        ASTHelpers.isSubtype(ASTHelpers.getResultType(tree), STREAM.get(state), state);
    String replacement =
        String.format(
            ".collect(%s()).block()%s", replacementMethodInvocation, isStream ? ".stream()" : "");
    return importSuggestion.merge(replaceMethodInvocation(tree, replacement, state)).build();
  }

  private static SuggestedFix.Builder replaceMethodInvocation(
      MethodInvocationTree tree, String replacement, VisitorState state) {
    int startPosition = state.getEndPosition(ASTHelpers.getReceiver(tree));
    int endPosition = state.getEndPosition(tree);

    checkState(
        startPosition != Position.NOPOS && endPosition != Position.NOPOS,
        "Cannot locate method to be replaced in source code");

    return SuggestedFix.builder().replace(startPosition, endPosition, replacement);
  }
}
