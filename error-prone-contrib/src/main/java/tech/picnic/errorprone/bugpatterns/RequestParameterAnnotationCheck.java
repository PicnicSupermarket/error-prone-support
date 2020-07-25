package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.methodHasParameters;
import static com.google.errorprone.matchers.Matchers.not;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

// XXX: TODOs for Stephan (besides the other XXXes):
// 1. Tweak documentation and class name.
// 2. Review tests.
/**
 * A {@link BugChecker} which flags Spring HTTP request parameter or body annotations as missing if
 * Spring HTTP request mapping annotations are present.
 *
 * <p>Matched mappings are {@code @{Delete,Get,Patch,Post,Put,Request}Mapping}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "RequestParameterAnnotation",
    summary = "Flag missing parameter annotations for Spring HTTP request mappings.",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.LIKELY_ERROR,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class RequestParameterAnnotationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ANN_PACKAGE_PREFIX = "org.springframework.web.bind.annotation.";
  private static final Matcher<Tree> HAS_MAPPING_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType(ANN_PACKAGE_PREFIX + "DeleteMapping"),
              isType(ANN_PACKAGE_PREFIX + "GetMapping"),
              isType(ANN_PACKAGE_PREFIX + "PatchMapping"),
              isType(ANN_PACKAGE_PREFIX + "PostMapping"),
              isType(ANN_PACKAGE_PREFIX + "PutMapping"),
              isType(ANN_PACKAGE_PREFIX + "RequestMapping")));
  // XXX: Add other parameters as necessary. See
  // https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-arguments.
  private static final Matcher<MethodTree> LACKS_PARAMETER_ANNOTATION =
      not(
          methodHasParameters(
              ALL,
              anyOf(
                  annotations(
                      AT_LEAST_ONE,
                      anyOf(
                          isType(ANN_PACKAGE_PREFIX + "PathVariable"),
                          isType(ANN_PACKAGE_PREFIX + "RequestBody"),
                          isType(ANN_PACKAGE_PREFIX + "RequestHeader"),
                          isType(ANN_PACKAGE_PREFIX + "RequestParam"),
                          isType("tech.picnic.webapp.JsonParam"))),
                  isSameType("java.io.InputStream"),
                  isSameType("javax.servlet.http.HttpServletRequest"),
                  isSameType("javax.servlet.http.HttpServletResponse"),
                  isSameType("org.springframework.http.HttpMethod"),
                  isSubtypeOf("org.springframework.web.context.request.WebRequest"))));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    // XXX: Auto-add `@RequestParam` where applicable.
    // XXX: What about the `PurchasingProposerRequestParams` in POM? Implies `@RequestBody`?
    // (Documentation doesn't mention this, IIUC.)
    return HAS_MAPPING_ANNOTATION.matches(tree, state)
            && LACKS_PARAMETER_ANNOTATION.matches(tree, state)
        ? buildDescription(tree)
            // XXX: Drop this `addFix`. (I added this to quickly find all violations in downstream
            // code using `./patch.sh RequestParameterAnnotation`.)
            .addFix(SuggestedFix.prefixWith(tree, "/* HIT */"))
            .setMessage(
                "Not all parameters of this request mapping method are annotated; this may be a mistake. "
                    + "If the unannotated parameters represent query string parameters, annotate them with `@RequestParam`.")
            .build()
        : Description.NO_MATCH;
  }
}
