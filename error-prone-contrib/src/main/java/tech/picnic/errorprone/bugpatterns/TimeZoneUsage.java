package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.enclosingClass;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
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
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

/** A {@link BugChecker} that flags illegal time-zone related operations. */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Derive the current time from an existing `Clock` Spring bean, and don't rely on a `Clock`'s time zone",
    link = BUG_PATTERNS_BASE_URL + "TimeZoneUsage",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class TimeZoneUsage extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> BANNED_TIME_METHOD =
      anyOf(
          allOf(
              instanceMethod()
                  .onDescendantOf(Clock.class.getCanonicalName())
                  .namedAnyOf("getZone", "withZone"),
              not(enclosingClass(isSubtypeOf(Clock.class)))),
          staticMethod()
              .onClass(Clock.class.getCanonicalName())
              .namedAnyOf(
                  "system",
                  "systemDefaultZone",
                  "systemUTC",
                  "tickMillis",
                  "tickMinutes",
                  "tickSeconds"),
          staticMethod()
              .onClassAny(
                  LocalDate.class.getCanonicalName(),
                  LocalDateTime.class.getCanonicalName(),
                  LocalTime.class.getCanonicalName(),
                  OffsetDateTime.class.getCanonicalName(),
                  OffsetTime.class.getCanonicalName(),
                  ZonedDateTime.class.getCanonicalName())
              .named("now"),
          staticMethod()
              .onClassAny(Instant.class.getCanonicalName())
              .named("now")
              .withNoParameters());

  /** Instantiates a new {@link TimeZoneUsage} instance. */
  public TimeZoneUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return BANNED_TIME_METHOD.matches(tree, state)
        ? buildDescription(tree).build()
        : Description.NO_MATCH;
  }
}
