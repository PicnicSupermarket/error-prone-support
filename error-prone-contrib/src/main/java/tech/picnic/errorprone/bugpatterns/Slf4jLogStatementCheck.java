package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;

import com.google.auto.service.AutoService;
import com.google.common.base.Splitter;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** A {@link BugChecker} which flags SLF4J usages that are likely to be in error. */
// XXX: The special-casing of Throwable applies only to SLF4J 1.6.0+; see
// https://www.slf4j.org/faq.html#paramException. That should be documented.
// XXX: Also simplify `LOG.error(String.format("Something %s", arg), throwable)`.
// XXX: Write a similar checker for Spring RestTemplates, String.format and friends, Guava
// preconditions, ...
@AutoService(BugChecker.class)
@BugPattern(
  name = "Slf4jLogStatement",
  summary = "Make sure SLF4J log statements contain proper placeholders with matching arguments",
  linkType = LinkType.NONE,
  severity = SeverityLevel.WARNING,
  tags = StandardTags.LIKELY_ERROR,
  providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION
)
public final class Slf4jLogStatementCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> MARKER = isSubtypeOf("org.slf4j.Marker");
  private static final Matcher<ExpressionTree> STRING = isSameType(String.class);
  private static final Matcher<ExpressionTree> THROWABLE = isSubtypeOf(Throwable.class);
  private static final Matcher<ExpressionTree> SLF4J_LOGGER_INVOCATION =
      instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .withNameMatching(Pattern.compile("trace|debug|info|warn|error"));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!SLF4J_LOGGER_INVOCATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> args = getTrimmedArguments(tree, state);
    Optional<String> formatString = getFormatString(args, state);
    if (!formatString.isPresent()) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);
    return validateFormatString(formatString.get(), args.get(0), state, description)
            && validateArguments(formatString.get(), args.subList(1, args.size()), description)
        ? Description.NO_MATCH
        : description.build();
  }

  private static List<? extends ExpressionTree> getTrimmedArguments(
      MethodInvocationTree tree, VisitorState state) {
    List<? extends ExpressionTree> args = tree.getArguments();
    verify(!args.isEmpty(), "Unexpected invocation of nullary SLF4J log method");
    /*
     * SLF4J log statements may accept a "marker" as a first argument, before the format string.
     * We ignore such markers.
     */
    int lTrim = MARKER.matches(args.get(0), state) ? 1 : 0;
    /*
     * SLF4J treats the final argument to a log statement specially if it is a `Throwabe`: it
     * will always choose to render the associated stacktrace, even if the argument has a
     * matching `{}` placeholder. (In this case the `{}` will simply be logged verbatim.) So for
     * the purpose of matching arguments against format string placeholders a trailing
     * `Throwable` effectively doesn't exist.
     */
    int rTrim = THROWABLE.matches(args.get(args.size() - 1), state) ? 1 : 0;
    return args.subList(lTrim, args.size() - rTrim);
  }

  private static Optional<String> getFormatString(
      List<? extends ExpressionTree> args, VisitorState state) {
    verify(!args.isEmpty(), "Failed to identify SLF4J log method format string");
    return Optional.ofNullable(ASTHelpers.constValue(args.get(0), String.class));
  }

  private static boolean validateFormatString(
      String formatString,
      ExpressionTree tree,
      VisitorState state,
      Description.Builder description) {
    String fixed = formatString.replace("%s", "{}");
    if (fixed.equals(formatString)) {
      return true;
    }

    description.setMessage("SLF4J log statement placeholders are of the form `{}`, not `%s`");
    if (tree.getKind() == Kind.STRING_LITERAL) {
      /*
       * We only suggest string replacement if the format string is a literal argument, cause
       * if the format string is a string constant defined elsewhere then it is not clear
       * whether the constant's definition must be updated or whether the constant should be
       * replaced at this usage site.
       */
      description.addFix(
          SuggestedFix.replace(tree, Util.treeToString(tree, state).replace("%s", "{}")));
    }

    return false;
  }

  private static boolean validateArguments(
      String formatString, List<? extends ExpressionTree> args, Description.Builder description) {
    int placeholders = Splitter.on("{}").splitToList(formatString).size() - 1;
    if (placeholders == args.size()) {
      return true;
    }

    description.setMessage(
        String.format(
            "Log statement contains %s placeholders, but specifies %s matching argument(s)",
            placeholders, args.size()));
    return false;
  }
}
