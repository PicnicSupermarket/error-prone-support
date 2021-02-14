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
    name = "TimeZoneUsage",
    summary =
        "Derive the current time from a `Clock` Spring bean, and don't rely on a `Clock`'s time zone",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.FRAGILE_CODE)
public final class TimeZoneUsageCheck extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> IS_BANNED_TIME_METHOD =
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
                  "tickSeconds"),
          staticMethod()
              .onClassAny(
                  Instant.class.getName(),
                  LocalDate.class.getName(),
                  LocalDateTime.class.getName(),
                  LocalTime.class.getName())
              .named("now"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!IS_BANNED_TIME_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "Derive the current time from an existing `Clock` Spring bean, and don't rely on a `Clock`'s time zone")
        .build();
  }
}
