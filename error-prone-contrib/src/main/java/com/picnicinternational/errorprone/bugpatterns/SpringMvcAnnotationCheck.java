package com.picnicinternational.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static java.util.stream.Collectors.joining;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree.Kind;
import java.util.Optional;

/**
 * A {@link BugChecker} which flags {@code @RequestMapping} annotations that can be written more
 * concisely.
 */
@AutoService(BugChecker.class)
@BugPattern(
  name = "SpringMvcAnnotation",
  summary =
      "Prefer the conciseness of `@{Get,Put,Post,Delete,Patch}Mapping` over `@RequestMapping`",
  linkType = LinkType.NONE,
  severity = SeverityLevel.SUGGESTION,
  tags = StandardTags.SIMPLIFICATION,
  providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION
)
public final class SpringMvcAnnotationCheck extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ANN_PACKAGE_PREFIX = "org.springframework.web.bind.annotation.";
  private static final String REQUEST_METHOD = ANN_PACKAGE_PREFIX + "RequestMethod";
  private static final AnnotationAttributeMatcher ARGUMENT_SELECTOR =
      AnnotationAttributeMatcher.create(
          Optional.of(ImmutableList.of(ANN_PACKAGE_PREFIX + "RequestMapping#method")),
          Optional.empty());
  private static final ImmutableMap<String, String> REPLACEMENTS =
      ImmutableMap.<String, String>builder()
          .put("DELETE", "DeleteMapping")
          .put("GET", "GetMapping")
          .put("PATCH", "PatchMapping")
          .put("POST", "PostMapping")
          .put("PUT", "PutMapping")
          .build();

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    // XXX: We could remove the `@RequestMapping` import if not other usages remain.
    return ARGUMENT_SELECTOR
        .extractMatchingArguments(tree)
        .findFirst()
        .flatMap(arg -> trySimplification(tree, arg))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> trySimplification(AnnotationTree tree, ExpressionTree arg) {
    return extractUniqueMethod(arg)
        .map(REPLACEMENTS::get)
        .map(newAnnotation -> replaceAnnotation(tree, arg, newAnnotation));
  }

  private static Optional<String> extractUniqueMethod(ExpressionTree arg) {
    // XXX: Drop the `new Object[] { }` wrapper once Error Prone depends on Guava 23.1+.
    verify(
        arg.getKind() == Kind.ASSIGNMENT,
        "Annotation attribute is not an assignment: %s",
        new Object[] {arg.getKind()});

    ExpressionTree expr = ((AssignmentTree) arg).getExpression();
    if (expr.getKind() != Kind.NEW_ARRAY) {
      return Optional.of(extractMethod(expr));
    }

    NewArrayTree newArray = (NewArrayTree) expr;
    return Optional.of(newArray.getInitializers())
        .filter(args -> args.size() == 1)
        .map(args -> extractMethod(args.get(0)));
  }

  private static String extractMethod(ExpressionTree expr) {
    switch (expr.getKind()) {
      case IDENTIFIER:
        return expr.toString();
      case MEMBER_SELECT:
        return ((MemberSelectTree) expr).getIdentifier().toString();
      default:
        throw new VerifyException("Unexpected type of expression: " + expr.getKind());
    }
  }

  private static Fix replaceAnnotation(
      AnnotationTree tree, ExpressionTree arg, String newAnnotation) {
    String newArguments =
        tree.getArguments()
            .stream()
            .filter(a -> !a.equals(arg))
            .map(Object::toString)
            .collect(joining(", "));

    return SuggestedFix.builder()
        .addImport(ANN_PACKAGE_PREFIX + newAnnotation)
        .replace(tree, String.format("@%s(%s)", newAnnotation, newArguments))
        .build();
  }
}
