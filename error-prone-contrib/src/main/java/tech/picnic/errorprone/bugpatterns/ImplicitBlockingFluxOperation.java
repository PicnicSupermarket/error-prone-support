package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
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
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Position;
import java.util.Optional;
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
    summary = "Accidental blocking of `Flux` with convenience method",
    explanation =
        "`Flux#toStream` and `Flux#toIterable` are documented to block, "
            + "but this is not apparent from the method signature; "
            + "please make sure that they are used with this in mind",
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
  private static final Supplier<Type> STREAM = Suppliers.typeFromString("java.util.stream.Stream");

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
      trySuggestFix("com.google.common.collect.ImmutableList.toImmutableList", tree, state)
          .ifPresent(description::addFix);
    }
    trySuggestFix("java.util.stream.Collectors.toUnmodifiableList", tree, state)
        .ifPresent(description::addFix);

    return description.build();
  }

  private static Optional<SuggestedFix> trySuggestFix(
      String fullyQualifiedMethodInvocation, MethodInvocationTree tree, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    String replacement =
        SuggestedFixes.qualifyStaticImport(fullyQualifiedMethodInvocation, fix, state);

    return replaceMethodInvocationWithCollect(tree, replacement + "()", fix.build(), state);
  }

  private static Optional<SuggestedFix> replaceMethodInvocationWithCollect(
      MethodInvocationTree tree,
      String collectArgument,
      SuggestedFix additionalFix,
      VisitorState state) {
    String collectMethodInvocation = String.format("collect(%s)", collectArgument);
    Types types = state.getTypes();
    String postFix =
        types.isSubtype(ASTHelpers.getResultType(tree), types.erasure(STREAM.get(state)))
            ? ".block().stream()"
            : ".block()";
    return replaceMethodInvocation(tree, collectMethodInvocation, state)
        .map(fix -> fix.merge(additionalFix))
        .map(fix -> fix.postfixWith(tree, postFix))
        .map(SuggestedFix.Builder::build);
  }

  private static Optional<SuggestedFix.Builder> replaceMethodInvocation(
      MethodInvocationTree tree, String replacement, VisitorState state) {
    ImmutableList<ErrorProneToken> tokens =
        ErrorProneTokens.getTokens(SourceCode.treeToString(tree, state), state.context);

    int treeStartPosition = ASTHelpers.getStartPosition(tree);
    int methodInvocationStartPosition =
        tokens.stream()
            .filter(
                token ->
                    token.hasName()
                        && token.name().equals(ASTHelpers.getSymbol(tree).getQualifiedName()))
            .findFirst()
            .map(token -> treeStartPosition + token.pos())
            .orElse(Position.NOPOS);

    if (treeStartPosition == Position.NOPOS || methodInvocationStartPosition == Position.NOPOS) {
      return Optional.empty();
    }

    int methodInvocationEndPosition = treeStartPosition + tokens.get(tokens.size() - 1).endPos();

    return Optional.of(
        SuggestedFix.builder()
            .replace(methodInvocationStartPosition, methodInvocationEndPosition, replacement));
  }
}
