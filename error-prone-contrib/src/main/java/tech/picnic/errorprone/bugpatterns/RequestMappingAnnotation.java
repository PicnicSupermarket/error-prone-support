package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.methodHasParameters;
import static com.google.errorprone.matchers.Matchers.not;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/**
 * A {@link BugChecker} which flags {@code @RequestMapping} methods that have one or more parameters
 * that appear to lack a relevant annotation.
 *
 * <p>Matched mappings are {@code @{Delete,Get,Patch,Post,Put,Request}Mapping}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Make sure all `@RequestMapping` method parameters are annotated",
    linkType = NONE,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class RequestMappingAnnotation extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ANN_PACKAGE_PREFIX = "org.springframework.web.bind.annotation.";
  // XXX: Generalize this logic to fully support Spring meta-annotations, then update the class
  // documentation.
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
  // XXX: Add other parameters as necessary. Also consider whether it makes sense to have WebMVC-
  // and WebFlux-specific logic. See
  // https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-arguments
  // and
  // https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-ann-arguments.
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
                          isType(ANN_PACKAGE_PREFIX + "RequestAttribute"))),
                  isSameType("java.io.InputStream"),
                  isSameType("java.time.ZoneId"),
                  isSameType("java.util.Locale"),
                  isSameType("java.util.TimeZone"),
                  isSameType("javax.servlet.http.HttpServletRequest"),
                  isSameType("javax.servlet.http.HttpServletResponse"),
                  isSameType("org.springframework.http.HttpMethod"),
                  isSameType("org.springframework.web.context.request.NativeWebRequest"),
                  isSameType("org.springframework.web.context.request.WebRequest"),
                  isSameType("org.springframework.web.server.ServerWebExchange"),
                  isSameType("org.springframework.web.util.UriBuilder"),
                  isSameType("org.springframework.web.util.UriComponentsBuilder"))));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    // XXX: Auto-add `@RequestParam` where applicable.
    // XXX: What about the `PurchasingProposerRequestParams` in POM? Implies `@RequestBody`?
    // (Documentation doesn't mention this, IIUC.)
    return HAS_MAPPING_ANNOTATION.matches(tree, state)
            && LACKS_PARAMETER_ANNOTATION.matches(tree, state)
        ? buildDescription(tree)
            .setMessage(
                "Not all parameters of this request mapping method are annotated; this may be a mistake. "
                    + "If the unannotated parameters represent query string parameters, annotate them with `@RequestParam`.")
            .build()
        : Description.NO_MATCH;
  }
}
