package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.Splitter;
import com.google.errorprone.BugPattern;
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
import tech.picnic.errorprone.utils.SourceCode;

/** A {@link BugChecker} that flags SLF4J usages that are likely to be in error. */
// XXX: The special-casing of Throwable applies only to SLF4J 1.6.0+; see
// https://www.slf4j.org/faq.html#paramException. That should be documented.
// XXX: Also simplify `LOG.error(String.format("Something %s", arg), throwable)`.
// XXX: Also simplify `LOG.error(String.join("sep", arg1, arg2), throwable)`? Perhaps too obscure.
// XXX: Write a similar checker for Spring RestTemplates, String.format and friends, Guava
// preconditions, ...
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Make sure SLF4J log statements contain proper placeholders with matching arguments",
    link = BUG_PATTERNS_BASE_URL + "Slf4jLogStatement",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class Slf4jLogStatement extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> SLF4J_MARKER = isSubtypeOf("org.slf4j.Marker");
  private static final Matcher<ExpressionTree> THROWABLE = isSubtypeOf(Throwable.class);
  private static final Matcher<ExpressionTree> SLF4J_LOGGER_INVOCATION =
      instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .namedAnyOf("trace", "debug", "info", "warn", "error");

  /** Instantiates a new {@link Slf4jLogStatement} instance. */
  public Slf4jLogStatement() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!SLF4J_LOGGER_INVOCATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> args = getTrimmedArguments(tree, state);
    return getFormatString(args)
        .map(formatString -> validateFormatString(formatString, tree, args, state))
        .orElse(Description.NO_MATCH);
  }

  private static List<? extends ExpressionTree> getTrimmedArguments(
      MethodInvocationTree tree, VisitorState state) {
    List<? extends ExpressionTree> args = tree.getArguments();
    verify(!args.isEmpty(), "Unexpected invocation of nullary SLF4J log method");
    /*
     * SLF4J log statements may accept a "marker" as a first argument, before the format string.
     * We ignore such markers.
     */
    int lTrim = SLF4J_MARKER.matches(args.get(0), state) ? 1 : 0;
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

  private static Optional<String> getFormatString(List<? extends ExpressionTree> args) {
    verify(!args.isEmpty(), "Failed to identify SLF4J log method format string");
    return Optional.ofNullable(ASTHelpers.constValue(args.get(0), String.class));
  }

  private Description validateFormatString(
      String formatString,
      MethodInvocationTree tree,
      List<? extends ExpressionTree> args,
      VisitorState state) {
    Description.Builder description = buildDescription(tree);
    return isFormatString(formatString, args.get(0), state, description)
            && hasValidArguments(formatString, args.subList(1, args.size()), description)
        ? Description.NO_MATCH
        : description.build();
  }

  private static boolean isFormatString(
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
          SuggestedFix.replace(tree, SourceCode.treeToString(tree, state).replace("%s", "{}")));
    }

    return false;
  }

  private static boolean hasValidArguments(
      CharSequence formatString,
      List<? extends ExpressionTree> args,
      Description.Builder description) {
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
