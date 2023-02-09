package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkState;
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
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Position;
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
    severity = SUGGESTION,
    tags = STYLE)
public final class ImplicitBlockingFlux extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLUX_WITH_IMPLICIT_BLOCK =
      instanceMethod()
          .onDescendantOf(Suppliers.typeFromString("reactor.core.publisher.Flux"))
          .namedAnyOf("toIterable", "toStream")
          .withNoParameters();
  private static final Supplier<Type> STREAM = Suppliers.typeFromString("java.util.stream.Stream");

  /** Instantiates a new {@link ImplicitBlockingFlux} instance. */
  public ImplicitBlockingFlux() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_WITH_IMPLICIT_BLOCK.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    description.addFix(SuggestedFixes.addSuppressWarnings(state, canonicalName()));
    if (ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
      description.addFix(
          trySuggestFix("com.google.common.collect.ImmutableList.toImmutableList", tree, state));
    }
    description.addFix(trySuggestFix("java.util.stream.Collectors.toList", tree, state));

    return description.build();
  }

  private static SuggestedFix trySuggestFix(
      String fullyQualifiedMethodInvocation, MethodInvocationTree tree, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    String replacement =
        SuggestedFixes.qualifyStaticImport(fullyQualifiedMethodInvocation, fix, state);

    return replaceMethodInvocationWithCollect(tree, replacement + "()", fix.build(), state);
  }

  private static SuggestedFix replaceMethodInvocationWithCollect(
      MethodInvocationTree tree,
      String collectArgument,
      SuggestedFix additionalFix,
      VisitorState state) {
    String collectMethodInvocation = String.format("collect(%s)", collectArgument);
    Types types = state.getTypes();
    String explicitBlock =
        types.isSubtype(ASTHelpers.getResultType(tree), types.erasure(STREAM.get(state)))
            ? ".block().stream()"
            : ".block()";
    return replaceMethodInvocation(tree, collectMethodInvocation, state)
        .merge(additionalFix)
        .postfixWith(tree, explicitBlock)
        .build();
  }

  private static SuggestedFix.Builder replaceMethodInvocation(
      MethodInvocationTree tree, String replacement, VisitorState state) {
    String methodName = ASTHelpers.getSymbol(tree).getQualifiedName().toString();

    int endPosition = state.getEndPosition(tree);
    checkState(endPosition != Position.NOPOS, "Cannot determine location of method in source code");
    int startPosition = endPosition - methodName.length() - 2;

    return SuggestedFix.builder().replace(startPosition, endPosition, replacement);
  }
}
