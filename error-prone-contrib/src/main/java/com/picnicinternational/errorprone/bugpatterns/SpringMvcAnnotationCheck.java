package com.picnicinternational.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;

import com.google.auto.service.AutoService;
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
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree.Kind;
import java.util.Optional;

@AutoService(BugChecker.class)
@BugPattern(
  name = "SpringMvcAnnotation",
  summary =
      "Prefer the conciseness of `@{Get,Put,Post,Delete,Patch}Mapping` over `@RequestMapping`",
  linkType = LinkType.NONE,
  severity = SeverityLevel.SUGGESTION,
  tags = StandardTags.STYLE,
  providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION
)
public class SpringMvcAnnotationCheck extends BugChecker implements AnnotationTreeMatcher {
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
    return ARGUMENT_SELECTOR
        .extractMatchingArguments(tree)
        .findFirst()
        .flatMap(arg -> trySimplification(tree, arg))
        .map(fix -> buildDescription(tree).addFix(fix).build())
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> trySimplification(AnnotationTree tree, ExpressionTree arg) {
    return extractUniqueMethod(arg)
        .map(REPLACEMENTS::get)
        .map(newAnnotation -> replaceAnnotation(tree, arg, newAnnotation));
  }

  private static Optional<String> extractUniqueMethod(ExpressionTree arg) {
    checkArgument(
        arg.getKind() == Kind.ASSIGNMENT,
        "Annotation attribute is not an assignment: %s",
        arg.getKind());

    ExpressionTree expr = ((AssignmentTree) arg).getExpression();
    if (expr.getKind() != Kind.NEW_ARRAY) {
      return extractMethod(expr);
    }

    NewArrayTree newArray = (NewArrayTree) expr;
    if (newArray.getInitializers().size() == 1) {
      return extractMethod(newArray.getInitializers().get(0));
    }

    return Optional.empty();
  }

  private static Optional<String> extractMethod(ExpressionTree expr) {
    if (expr.getKind() == Kind.IDENTIFIER) {
      // XXX: Not quite correct. This _could_ be some custom constant...
      return Optional.of(expr.toString());
    }

    if (expr.getKind() != Kind.MEMBER_SELECT) {
      return Optional.empty();
    }

    MemberSelectTree memberSelect = (MemberSelectTree) expr;
    if (!REQUEST_METHOD.equals(String.valueOf(ASTHelpers.getType(memberSelect.getExpression())))) {
      return Optional.empty();
    }

    return Optional.of(memberSelect.getIdentifier().toString());
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
