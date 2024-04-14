package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.Collections;
import java.util.Formattable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags {@link String#format(String, Object...)}, {@link
 * String#format(Locale, String, Object...)} and {@link String#formatted(Object...)} invocations
 * that can be omitted by delegating to another format method.
 */
// XXX: The special-casing of Throwable applies only to SLF4J 1.6.0+; see
// https://www.slf4j.org/faq.html#paramException. That should be documented.
// XXX: Some of the `Matcher`s defined here are also declared by the `Slf4jLogStatement` and
// `RedundantStringConversion` checks. Look into deduplicating them.
// XXX: Should we also simplify e.g. `LOG.error(String.join("sep", arg1, arg2), throwable)`? Perhaps
// that's too obscure.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "String formatting can be deferred",
    link = BUG_PATTERNS_BASE_URL + "EagerStringFormatting",
    linkType = CUSTOM,
    severity = WARNING,
    tags = {PERFORMANCE, SIMPLIFICATION})
public final class EagerStringFormatting extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FORMATTABLE = isSubtypeOf(Formattable.class);
  private static final Matcher<ExpressionTree> LOCALE = isSubtypeOf(Locale.class);
  private static final Matcher<ExpressionTree> SLF4J_MARKER = isSubtypeOf("org.slf4j.Marker");
  private static final Matcher<ExpressionTree> THROWABLE = isSubtypeOf(Throwable.class);
  private static final Matcher<ExpressionTree> REQUIRE_NOT_NULL_INVOCATION =
      staticMethod().onClass(Objects.class.getCanonicalName()).named("requireNonNull");
  private static final Matcher<ExpressionTree> GUAVA_GUARD_INVOCATION =
      anyOf(
          staticMethod()
              .onClass(Preconditions.class.getCanonicalName())
              .namedAnyOf("checkArgument", "checkNotNull", "checkState"),
          staticMethod()
              .onClass(Verify.class.getCanonicalName())
              .namedAnyOf("verify", "verifyNotNull"));
  private static final Matcher<ExpressionTree> SLF4J_LOGGER_INVOCATION =
      instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .namedAnyOf("trace", "debug", "info", "warn", "error");
  private static final Matcher<ExpressionTree> STATIC_FORMAT_STRING =
      staticMethod().onClass(String.class.getCanonicalName()).named("format");
  private static final Matcher<ExpressionTree> INSTANCE_FORMAT_STRING =
      instanceMethod().onDescendantOf(String.class.getCanonicalName()).named("formatted");

  /** Instantiates a new {@link EagerStringFormatting} instance. */
  public EagerStringFormatting() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    Tree parent = state.getPath().getParentPath().getLeaf();
    if (!(parent instanceof MethodInvocationTree methodInvocation)) {
      /*
       * Fast path: this isn't a method invocation whose result is an argument to another method
       * invocation. (We assume that this expression isn't redundantly wrapped in parentheses.)
       */
      return Description.NO_MATCH;
    }

    return StringFormatExpression.tryCreate(tree, state)
        .map(expr -> analyzeFormatStringContext(expr, methodInvocation, state))
        .orElse(Description.NO_MATCH);
  }

  private Description analyzeFormatStringContext(
      StringFormatExpression stringFormat, MethodInvocationTree context, VisitorState state) {
    List<? extends ExpressionTree> arguments = context.getArguments();

    if (REQUIRE_NOT_NULL_INVOCATION.matches(context, state)) {
      if (arguments.size() != 2) {
        /* Vacuous validation that string formatting doesn't yield `null`. */
        // XXX: Maybe flag this, while we're here?
        return Description.NO_MATCH;
      }

      if (stringFormat.arguments().stream()
          .anyMatch(EagerStringFormatting::isNonFinalLocalVariable)) {
        /*
         * The format operation depends on a variable that isn't final or effectively final; moving
         * it into a lambda expression would cause a compilation error.
         */
        // XXX: Define a custom message for this case?
        return describeMatch(context);
      }

      /*
       * Suggest that the string formatting is deferred. Note that moving the format operation into
       * a lambda expression will cause a compilation in case any of its arguments aren't
       * effectively final.
       */
      return describeMatch(context, SuggestedFix.prefixWith(stringFormat.expression(), "() -> "));
    }

    if (GUAVA_GUARD_INVOCATION.matches(context, state)) {
      if (arguments.size() < 2) {
        /* Vacuous validation that string formatting doesn't yield `null`. */
        // XXX: Maybe flag this, while we're here?
        return Description.NO_MATCH;
      }

      if (stringFormat.simplifiableFormatString().isEmpty()) {
        /* We can't simplify this case; only flag it. */
        // XXX: Use a custom message.
        return describeMatch(context);
      }

      // XXX: Collapse into previous statement? (Depends on the extent to which we'll customize
      // messages.)
      if (arguments.size() > 2) {
        /*
         * The format string produces a format string itself, or its result is the input to another
         * format operation. This is a complex case that we'll only flag.
         */
        // XXX: Use a custom message.
        return describeMatch(context);
      }

      return describeMatch(context, stringFormat.suggestFlattening("%s", state));
    }

    if (SLF4J_LOGGER_INVOCATION.matches(context, state)) {
      if (stringFormat.simplifiableFormatString().isEmpty()) {
        /* We can't simplify this case; only flag it. */
        // XXX: Use a custom message.
        return describeMatch(context);
      }

      int leftOffset = SLF4J_MARKER.matches(arguments.get(0), state) ? 1 : 0;
      int rightOffset = THROWABLE.matches(arguments.get(arguments.size() - 1), state) ? 1 : 0;
      if (arguments.size() != leftOffset + 1 + rightOffset) {
        /*
         * The format string produces a format string itself, or its result is the input to another
         * format operation. This is a complex case that we'll only flag.
         */
        // XXX: Use a custom message.
        return describeMatch(context);
      }

      return describeMatch(context, stringFormat.suggestFlattening("{}", state));
    }

    /*
     * The string formatting operation does not appear to happen in a context that admits of
     * simplification or optimization.
     */
    return Description.NO_MATCH;
  }

  private static boolean isNonFinalLocalVariable(Tree tree) {
    Symbol symbol = ASTHelpers.getSymbol(tree);
    return symbol instanceof VarSymbol
        && symbol.owner instanceof MethodSymbol
        && !ASTHelpers.isConsideredFinal(symbol);
  }

  /** Description of a string format expression. */
  @AutoValue
  abstract static class StringFormatExpression {
    /** The full string format expression. */
    abstract Tree expression();

    /** The string format arguments to be plugged into its format string. */
    abstract ImmutableList<ExpressionTree> arguments();

    /**
     * The constant format string, if it contains only {@code %s} placeholders, and the number of
     * said placeholders matches the number of format arguments.
     */
    abstract Optional<String> simplifiableFormatString();

    private SuggestedFix suggestFlattening(String newPlaceholder, VisitorState state) {
      return SuggestedFix.replace(
          expression(),
          Stream.concat(
                  Stream.of(deriveFormatStringExpression(newPlaceholder, state)),
                  arguments().stream().map(arg -> SourceCode.treeToString(arg, state)))
              .collect(joining(", ")));
    }

    final String deriveFormatStringExpression(String newPlaceholder, VisitorState state) {
      return SourceCode.toStringConstantExpression(
          String.format(
              simplifiableFormatString()
                  .orElseThrow(
                      () -> new IllegalStateException("Format string cannot be simplified")),
              Collections.nCopies(arguments().size(), newPlaceholder).toArray()),
          state);
    }

    private static Optional<StringFormatExpression> tryCreate(
        MethodInvocationTree tree, VisitorState state) {
      if (INSTANCE_FORMAT_STRING.matches(tree, state)) {
        return Optional.of(
            create(
                tree,
                ASTHelpers.getReceiver(tree),
                ImmutableList.copyOf(tree.getArguments()),
                state));
      }

      if (STATIC_FORMAT_STRING.matches(tree, state)) {
        List<? extends ExpressionTree> arguments = tree.getArguments();
        int argOffset = LOCALE.matches(arguments.get(0), state) ? 1 : 0;
        return Optional.of(
            create(
                tree,
                arguments.get(argOffset),
                ImmutableList.copyOf(arguments.subList(argOffset + 1, arguments.size())),
                state));
      }

      return Optional.empty();
    }

    private static StringFormatExpression create(
        Tree expression,
        Tree formatString,
        ImmutableList<ExpressionTree> arguments,
        VisitorState state) {
      return new AutoValue_EagerStringFormatting_StringFormatExpression(
          expression,
          arguments,
          Optional.ofNullable(ASTHelpers.constValue(formatString, String.class))
              .filter(template -> isSimplifiable(template, arguments, state)));
    }

    private static boolean isSimplifiable(
        String formatString, ImmutableList<ExpressionTree> arguments, VisitorState state) {
      if (arguments.stream().anyMatch(arg -> FORMATTABLE.matches(arg, state))) {
        /* `Formattable` arguments can have arbitrary format semantics. */
        return false;
      }

      @Var int placeholderCount = 0;
      for (int p = formatString.indexOf('%'); p != -1; p = formatString.indexOf('%', p + 2)) {
        if (p == formatString.length() - 1) {
          /* Malformed format string with trailing `%`. */
          return false;
        }

        char modifier = formatString.charAt(p + 1);
        if (modifier == 's') {
          placeholderCount++;
        } else if (modifier != '%') {
          /* Only `%s` and `%%` (a literal `%`) are supported. */
          return false;
        }
      }

      return placeholderCount == arguments.size();
    }
  }
}
