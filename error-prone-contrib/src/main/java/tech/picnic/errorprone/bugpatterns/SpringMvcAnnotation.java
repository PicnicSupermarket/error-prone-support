package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import java.util.Optional;
import tech.picnic.errorprone.utils.AnnotationAttributeMatcher;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags {@code @RequestMapping} annotations that can be written more
 * concisely.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Prefer the conciseness of `@{Get,Put,Post,Delete,Patch}Mapping` over `@RequestMapping`",
    link = BUG_PATTERNS_BASE_URL + "SpringMvcAnnotation",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class SpringMvcAnnotation extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ANN_PACKAGE_PREFIX = "org.springframework.web.bind.annotation.";
  private static final AnnotationAttributeMatcher ARGUMENT_SELECTOR =
      AnnotationAttributeMatcher.create(
          Optional.of(ImmutableList.of(ANN_PACKAGE_PREFIX + "RequestMapping#method")),
          ImmutableList.of());
  private static final ImmutableMap<String, String> REPLACEMENTS =
      ImmutableMap.<String, String>builder()
          .put("DELETE", "DeleteMapping")
          .put("GET", "GetMapping")
          .put("PATCH", "PatchMapping")
          .put("POST", "PostMapping")
          .put("PUT", "PutMapping")
          .build();

  /** Instantiates a new {@link SpringMvcAnnotation} instance. */
  public SpringMvcAnnotation() {}

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    // XXX: We could remove the `@RequestMapping` import if not other usages remain.
    return ARGUMENT_SELECTOR
        .extractMatchingArguments(tree)
        .findFirst()
        .flatMap(arg -> trySimplification(tree, arg, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> trySimplification(
      AnnotationTree tree, ExpressionTree arg, VisitorState state) {
    return extractUniqueMethod(arg, state)
        .map(REPLACEMENTS::get)
        .map(newAnnotation -> replaceAnnotation(tree, arg, newAnnotation, state));
  }

  private static Optional<String> extractUniqueMethod(ExpressionTree arg, VisitorState state) {
    if (!(arg instanceof AssignmentTree assignment)) {
      throw new VerifyException("Annotation attribute is not an assignment:" + arg.getKind());
    }

    ExpressionTree expr = assignment.getExpression();
    return expr instanceof NewArrayTree newArray
        ? Optional.of(newArray.getInitializers())
            .filter(args -> args.size() == 1)
            .map(args -> extractMethod(args.get(0), state))
        : Optional.of(extractMethod(expr, state));
  }

  // XXX: Use switch pattern matching once the targeted JDK supports this.
  private static String extractMethod(ExpressionTree expr, VisitorState state) {
    return switch (expr.getKind()) {
      case IDENTIFIER -> SourceCode.treeToString(expr, state);
      case MEMBER_SELECT -> ((MemberSelectTree) expr).getIdentifier().toString();
      default -> throw new VerifyException("Unexpected type of expression: " + expr.getKind());
    };
  }

  private static Fix replaceAnnotation(
      AnnotationTree tree, ExpressionTree argToRemove, String newAnnotation, VisitorState state) {
    String newArguments =
        tree.getArguments().stream()
            .filter(not(argToRemove::equals))
            .map(arg -> SourceCode.treeToString(arg, state))
            .collect(joining(", "));

    SuggestedFix.Builder fix = SuggestedFix.builder();
    String annotation = SuggestedFixes.qualifyType(state, fix, ANN_PACKAGE_PREFIX + newAnnotation);
    return fix.replace(tree, String.format("@%s(%s)", annotation, newArguments)).build();
  }
}
