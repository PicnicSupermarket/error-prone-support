package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.function.Predicate.not;

import com.google.auto.common.AnnotationMirrors;
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
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.predicates.TypePredicates;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationValue;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} which flags annotations that could be written more concisely. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Omit redundant syntax from annotation declarations",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class CanonicalAnnotationSyntax extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Pattern TRAILING_ARRAY_COMMA = Pattern.compile(",\\s*}$");
  private static final ImmutableSet<BiFunction<AnnotationTree, VisitorState, Optional<Fix>>>
      FIX_FACTORIES =
          ImmutableSet.of(
              CanonicalAnnotationSyntax::useSpringAlias,
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

  private static Optional<Fix> useSpringAlias(AnnotationTree tree, VisitorState state) {
    boolean canDropAttributeName = tree.getArguments().size() == 1;

    // XXX: The reduce is repeated. Fix.
    return tree.getArguments().stream()
        .flatMap(arg -> useBetterAlias(arg, canDropAttributeName, state).stream())
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build);
  }

  private static Optional<SuggestedFix.Builder> useBetterAlias(
      ExpressionTree tree, boolean canDropAttributeName, VisitorState state) {
    if (tree.getKind() != Kind.ASSIGNMENT) {
      return Optional.empty();
    }

    AssignmentTree assignment = (AssignmentTree) tree;
    ExpressionTree element = assignment.getVariable();
    if (canDropAttributeName == "value".equals(SourceCode.treeToString(element, state))) {
      return Optional.empty();
    }

    return getAlias(element, state)
        .map(
            alias ->
                "value".equals(alias)
                    ? SuggestedFix.builder()
                        .replace(
                            assignment, SourceCode.treeToString(assignment.getExpression(), state))
                    : SuggestedFix.builder().replace(element, alias));
  }

  // XXX: Also add support for non-default `AliasFor#annotation` values!
  private static Optional<String> getAlias(ExpressionTree tree, VisitorState state) {
    Symbol sym = ASTHelpers.getSymbol(tree);
    if (sym == null) {
      return Optional.empty();
    }

    // XXX: Extract.
    TypePredicate isAliasFor =
        TypePredicates.isExactType("org.springframework.core.annotation.AliasFor");
    TypePredicate isAnnotation = TypePredicates.isExactType("java.lang.annotation.Annotation");

    return sym.getAnnotationMirrors().stream()
        .filter(m -> isAliasFor.apply(m.type, state))
        .filter(
            m ->
                isAnnotation.apply(
                    ((Attribute.Class) AnnotationMirrors.getAnnotationValue(m, "annotation"))
                        .classType,
                    state))
        .flatMap(
            m ->
                Stream.of(
                    AnnotationMirrors.getAnnotationValue(m, "value"),
                    AnnotationMirrors.getAnnotationValue(m, "attribute")))
        .map(AnnotationValue::getValue)
        .map(Object::toString)
        .filter(not(String::isEmpty))
        .findFirst();
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
