package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that flags annotations that could be written more concisely. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Omit redundant syntax from annotation declarations",
    link = BUG_PATTERNS_BASE_URL + "CanonicalAnnotationSyntax",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class CanonicalAnnotationSyntax extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Pattern TRAILING_ARRAY_COMMA = Pattern.compile(",\\s*}$");
  private static final ImmutableSet<BiFunction<AnnotationTree, VisitorState, Optional<Fix>>>
      FIX_FACTORIES =
          ImmutableSet.of(
              CanonicalAnnotationSyntax::dropRedundantParentheses,
              CanonicalAnnotationSyntax::dropRedundantValueAttribute,
              CanonicalAnnotationSyntax::dropRedundantCurlies);

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    return FIX_FACTORIES.stream()
        .map(op -> op.apply(tree, state))
        .flatMap(Optional::stream)
        .findFirst()
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> dropRedundantParentheses(AnnotationTree tree, VisitorState state) {
    if (!tree.getArguments().isEmpty()) {
      /* Parentheses are necessary. */
      return Optional.empty();
    }

    String src = state.getSourceForNode(tree);
    if (src == null) {
      /* Without the source code there's not much we can do. */
      return Optional.empty();
    }

    int parenIndex = src.indexOf('(');
    if (parenIndex < 0) {
      /* There are no redundant parentheses. */
      return Optional.empty();
    }

    return Optional.of(SuggestedFix.replace(tree, src.substring(0, parenIndex)));
  }

  private static Optional<Fix> dropRedundantValueAttribute(
      AnnotationTree tree, VisitorState state) {
    List<? extends ExpressionTree> args = tree.getArguments();
    if (args.size() != 1) {
      /* The `value` attribute, if specified, cannot be dropped. */
      return Optional.empty();
    }

    ExpressionTree arg = args.get(0);
    if (state.getSourceForNode(arg) == null) {
      /*
       * The annotation argument isn't doesn't have a source representation, e.g. because `value`
       * isn't assigned to explicitly.
       */
      return Optional.empty();
    }

    ExpressionTree expr = AnnotationMatcherUtils.getArgument(tree, "value");
    if (expr == null) {
      /* This is not an explicit assignment to the `value` attribute. */
      return Optional.empty();
    }

    /* Replace the assignment with (the simplified representation of) just its value. */
    return Optional.of(
        SuggestedFix.replace(
            arg,
            simplifyAttributeValue(expr, state)
                .orElseGet(() -> SourceCode.treeToString(expr, state))));
  }

  private static Optional<Fix> dropRedundantCurlies(AnnotationTree tree, VisitorState state) {
    List<SuggestedFix.Builder> fixes = new ArrayList<>();
    for (ExpressionTree arg : tree.getArguments()) {
      /*
       * We'll try to simplify each assignment's RHS; for non-assignment we'll try to simplify
       * the expression as a whole.
       */
      ExpressionTree value =
          (arg.getKind() == Kind.ASSIGNMENT) ? ((AssignmentTree) arg).getExpression() : arg;

      /* Store a fix for each expression that was successfully simplified. */
      simplifyAttributeValue(value, state)
          .ifPresent(expr -> fixes.add(SuggestedFix.builder().replace(value, expr)));
    }

    return fixes.stream().reduce(SuggestedFix.Builder::merge).map(SuggestedFix.Builder::build);
  }

  private static Optional<String> simplifyAttributeValue(ExpressionTree expr, VisitorState state) {
    if (expr.getKind() != Kind.NEW_ARRAY) {
      /* There are no curly braces or commas to be dropped here. */
      return Optional.empty();
    }

    NewArrayTree array = (NewArrayTree) expr;
    return simplifySingletonArray(array, state).or(() -> dropTrailingComma(array, state));
  }

  /** Returns the expression describing the array's sole element, if any. */
  private static Optional<String> simplifySingletonArray(NewArrayTree array, VisitorState state) {
    return Optional.of(array.getInitializers())
        .filter(initializers -> initializers.size() == 1)
        .map(initializers -> SourceCode.treeToString(initializers.get(0), state));
  }

  private static Optional<String> dropTrailingComma(NewArrayTree array, VisitorState state) {
    String src = SourceCode.treeToString(array, state);
    return Optional.of(TRAILING_ARRAY_COMMA.matcher(src))
        .filter(Matcher::find)
        .map(m -> src.substring(0, m.start()) + '}');
  }
}
