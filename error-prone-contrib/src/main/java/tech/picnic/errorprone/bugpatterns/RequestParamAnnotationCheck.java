package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.methodHasParameters;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;

/**
 * A {@link BugChecker} which flags {@code RequestParam} parameters that have an invalid type.
 *
 * <p>Types considered invalid are {@link ImmutableMap} and subtypes of {@link ImmutableCollection}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "RequestParamAnnotationCheck",
    summary = "Make sure all `@RequestParam` method parameters are valid",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class RequestParamAnnotationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ANN_PACKAGE_PREFIX = "org.springframework.web.bind.annotation.";

  private static final Matcher<MethodTree> HAS_REQUEST_MAPPING_ANNOTATION =
      allOf(
          annotations(
              ALL,
              anyOf(
                  isType(ANN_PACKAGE_PREFIX + "DeleteMapping"),
                  isType(ANN_PACKAGE_PREFIX + "GetMapping"),
                  isType(ANN_PACKAGE_PREFIX + "PatchMapping"),
                  isType(ANN_PACKAGE_PREFIX + "PostMapping"),
                  isType(ANN_PACKAGE_PREFIX + "PutMapping"),
                  isType(ANN_PACKAGE_PREFIX + "RequestMapping"))),
          methodHasParameters(
              AT_LEAST_ONE,
              annotations(AT_LEAST_ONE, isType(ANN_PACKAGE_PREFIX + "RequestParam"))));

  private static final Matcher<MethodTree> HAS_INVALID_REQUEST_PARAM_TYPE =
      methodHasParameters(
          AT_LEAST_ONE,
          allOf(
              annotations(ALL, isType(ANN_PACKAGE_PREFIX + "RequestParam")),
              anyOf(
                  isSubtypeOf("com.google.common.collect.ImmutableCollection"),
                  isSameType("com.google.common.collect.ImmutableMap"))));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    return HAS_REQUEST_MAPPING_ANNOTATION.matches(tree, state)
            && HAS_INVALID_REQUEST_PARAM_TYPE.matches(tree, state)
        ? buildDescription(tree)
            .setMessage(
                "At least one defined Request Parameter has an invalid type. "
                    + "`ImmutableMap`, `ImmutableCollection` and subtypes are not allowed.")
            .build()
        : Description.NO_MATCH;
  }
}
