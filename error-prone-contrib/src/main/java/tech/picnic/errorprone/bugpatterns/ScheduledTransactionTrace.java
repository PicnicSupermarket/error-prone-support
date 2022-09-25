package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/**
 * A {@link BugChecker} that flags methods with Spring's {@code @Scheduled} annotation that lack New
 * Relic Agent's {@code @Trace(dispatcher = true)}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Scheduled operation must start a new New Relic transaction",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class ScheduledTransactionTrace extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String TRACE_ANNOTATION_FQCN = "com.newrelic.api.agent.Trace";
  private static final Matcher<Tree> IS_SCHEDULED =
      hasAnnotation("org.springframework.scheduling.annotation.Scheduled");
  private static final MultiMatcher<Tree, AnnotationTree> TRACE_ANNOTATION =
      annotations(AT_LEAST_ONE, isType(TRACE_ANNOTATION_FQCN));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!IS_SCHEDULED.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ImmutableList<AnnotationTree> traceAnnotations =
        TRACE_ANNOTATION.multiMatchResult(tree, state).matchingNodes();
    if (traceAnnotations.isEmpty()) {
      /* This method completely lacks the `@Trace` annotation; add it. */
      return describeMatch(
          tree,
          SuggestedFix.builder()
              .addImport(TRACE_ANNOTATION_FQCN)
              .prefixWith(tree, "@Trace(dispatcher = true)")
              .build());
    }

    AnnotationTree traceAnnotation = Iterables.getOnlyElement(traceAnnotations);
    if (isCorrectAnnotation(traceAnnotation)) {
      return Description.NO_MATCH;
    }

    /*
     * The `@Trace` annotation is present but does not specify `dispatcher = true`. Add or update
     * the `dispatcher` annotation element.
     */
    return describeMatch(
        traceAnnotation,
        SuggestedFixes.updateAnnotationArgumentValues(
                traceAnnotation, state, "dispatcher", ImmutableList.of("true"))
            .build());
  }

  private static boolean isCorrectAnnotation(AnnotationTree traceAnnotation) {
    return Boolean.TRUE.equals(
        AnnotationMirrors.getAnnotationValue(
                ASTHelpers.getAnnotationMirror(traceAnnotation), "dispatcher")
            .getValue());
  }
}
