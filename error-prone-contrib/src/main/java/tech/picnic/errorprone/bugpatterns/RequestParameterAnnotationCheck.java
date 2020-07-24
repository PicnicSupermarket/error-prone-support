package tech.picnic.errorprone.bugpatterns;

import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;

/**
 * A {@link BugChecker} which flags Spring HTTP request parameter or body annotations as missing if
 * Spring HTTP request mapping annotations are present.
 *
 * <p>Matched mappings are {@code @{Get,Put,Post,Delete,Patch}Mapping}. {@code @RequestMapping}
 * mappings are already covered by {@link SpringMvcAnnotationCheck}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "RequestParameterAnnotationCheck",
    summary = "Flag missing parameter annotations for Spring HTTP request mappings.",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.LIKELY_ERROR,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class RequestParameterAnnotationCheck extends BugChecker
    implements BugChecker.MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ANN_PACKAGE_PREFIX = "org.springframework.web.bind.annotation.";
  private static final ImmutableList<String> MAPPING_ANNOTATIONS =
      ImmutableList.of(
          ANN_PACKAGE_PREFIX + "DeleteMapping",
          ANN_PACKAGE_PREFIX + "GetMapping",
          ANN_PACKAGE_PREFIX + "PatchMapping",
          ANN_PACKAGE_PREFIX + "PostMapping",
          ANN_PACKAGE_PREFIX + "PutMapping");
  private static final ImmutableList<String> PARAMETER_ANNOTATIONS =
      ImmutableList.of(
          ANN_PACKAGE_PREFIX + "PathVariable",
          ANN_PACKAGE_PREFIX + "RequestBody",
          ANN_PACKAGE_PREFIX + "RequestParam");

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    return Optional.of(tree)
        .filter(RequestParameterAnnotationCheck::matchesAnyMappingAnnotation)
        .filter(not(RequestParameterAnnotationCheck::matchesAllParameters))
        .map(this::describeMatch)
        .orElse(Description.NO_MATCH);
  }

  private static boolean matchesAnyMappingAnnotation(MethodTree tree) {
    return tree.getModifiers().getAnnotations().stream()
        .anyMatch(RequestParameterAnnotationCheck::matchesMappingAnnotation);
  }

  private static boolean matchesAllParameters(MethodTree tree) {
    return tree.getParameters().stream()
        .allMatch(RequestParameterAnnotationCheck::matchAnyParameterAnnotation);
  }

  private static boolean matchAnyParameterAnnotation(VariableTree t) {
    return t.getModifiers().getAnnotations().stream()
        .anyMatch(RequestParameterAnnotationCheck::matchesParameterAnnotation);
  }

  private static boolean matchesMappingAnnotation(AnnotationTree tree) {
    return matchGivenAnnotations(tree, MAPPING_ANNOTATIONS);
  }

  private static boolean matchesParameterAnnotation(AnnotationTree tree) {
    return matchGivenAnnotations(tree, PARAMETER_ANNOTATIONS);
  }

  private static boolean matchGivenAnnotations(
      AnnotationTree tree, ImmutableList<String> mappingAnnotations) {
    return Optional.ofNullable(ASTHelpers.getType(tree.getAnnotationType()))
        .map(Type::toString)
        .filter(mappingAnnotations::contains)
        .isPresent();
  }
}
