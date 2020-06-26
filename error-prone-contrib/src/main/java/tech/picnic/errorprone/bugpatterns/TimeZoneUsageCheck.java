package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** A {@link BugChecker} which flags illegal time-zone related operations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "TimeZoneUsageCheck",
    summary = "Avoid illegal operations on assorted time related objects",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.FRAGILE_CODE)
public final class TimeZoneUsageCheck extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> ILLEGAL_CLOCK_USAGES =
      anyOf(
          instanceMethod().onDescendantOf(Clock.class.getName()).namedAnyOf("getZone", "withZone"),
          staticMethod()
              .onClass(Clock.class.getName())
              .namedAnyOf(
                  "system",
                  "systemDefaultZone",
                  "systemUTC",
                  "tickMillis",
                  "tickMinutes",
                  "tickSeconds"));
  private static final Matcher<ExpressionTree> NOW_USAGES =
      anyOf(
          staticMethod()
              .onClassAny(
                  Instant.class.getName(),
                  LocalDate.class.getName(),
                  LocalDateTime.class.getName(),
                  LocalTime.class.getName())
              .namedAnyOf("now"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ILLEGAL_CLOCK_USAGES.matches(tree, state) && !NOW_USAGES.matches(tree, state)) {
      return Description.NO_MATCH;
    }
    return buildDescription(tree)
        .setMessage("This operation is not recommended, please refactor it")
        .build();
  }
}
