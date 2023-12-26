package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Constants;
import java.util.Formattable;
import java.util.Iterator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags {@link String#format(String, Object...)} invocations which can be
 * replaced with a {@link String#join(CharSequence, CharSequence...)} or even a {@link
 * String#valueOf} invocation.
 */
// XXX: What about `v1 + "sep" + v2` and similar expressions? Do we want to rewrite those to
// `String.join`, or should some `String.join` invocations be rewritten to use the `+` operator?
// (The latter suggestion would conflict with the `FormatStringConcatenation` check.)
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `String#join` over `String#format`",
    link = BUG_PATTERNS_BASE_URL + "StringJoin",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class StringJoin extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Splitter FORMAT_SPECIFIER_SPLITTER = Splitter.on("%s");
  private static final Matcher<ExpressionTree> STRING_FORMAT_INVOCATION =
      staticMethod().onClass(String.class.getCanonicalName()).named("format");
  private static final Supplier<Type> CHAR_SEQUENCE_TYPE =
      Suppliers.typeFromClass(CharSequence.class);
  private static final Supplier<Type> FORMATTABLE_TYPE = Suppliers.typeFromClass(Formattable.class);

  /** Instantiates a new {@link StringJoin} instance. */
  public StringJoin() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!STRING_FORMAT_INVOCATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    // XXX: This check assumes that if the first argument to `String#format` is a `Locale`, that
    // this argument is not vacuous, and that as a result the expression cannot be simplified using
    // `#valueOf` or `#join`. Implement a separate check that identifies and drops redundant
    // `Locale` arguments. See also a related comment in `FormatStringConcatenation`.
    String formatString = ASTHelpers.constValue(tree.getArguments().get(0), String.class);
    if (formatString == null) {
      return Description.NO_MATCH;
    }

    List<String> separators = FORMAT_SPECIFIER_SPLITTER.splitToList(formatString);
    if (separators.size() < 2) {
      /* The format string does not contain `%s` format specifiers. */
      return Description.NO_MATCH;
    }

    if (separators.size() != tree.getArguments().size()) {
      /* The number of arguments does not match the number of `%s` format specifiers. */
      return Description.NO_MATCH;
    }

    int lastIndex = separators.size() - 1;
    if (!separators.get(0).isEmpty() || !separators.get(lastIndex).isEmpty()) {
      /* The format string contains leading or trailing characters. */
      return Description.NO_MATCH;
    }

    ImmutableSet<String> innerSeparators = ImmutableSet.copyOf(separators.subList(1, lastIndex));
    if (innerSeparators.size() > 1) {
      /* The `%s` format specifiers are not uniformly separated. */
      return Description.NO_MATCH;
    }

    if (innerSeparators.isEmpty()) {
      /*
       * This `String#format` invocation performs a straightforward string conversion; use
       * `String#valueOf` instead.
       */
      return trySuggestExplicitStringConversion(tree, state);
    }

    String separator = Iterables.getOnlyElement(innerSeparators);
    if (separator.indexOf('%') >= 0) {
      /* The `%s` format specifiers are separated by another format specifier. */
      // XXX: Strictly speaking we could support `%%` by mapping it to a literal `%`, but that
      // doesn't seem worth the trouble.
      return Description.NO_MATCH;
    }

    return trySuggestExplicitJoin(tree, separator, state);
  }

  /**
   * If guaranteed to be behavior preserving, suggests replacing {@code String.format("%s", arg)}
   * with {@code String.valueOf(arg)}.
   *
   * <p>If {@code arg} is already a string then the resultant conversion is vacuous. The {@link
   * IdentityConversion} check will subsequently drop it.
   */
  private Description trySuggestExplicitStringConversion(
      MethodInvocationTree tree, VisitorState state) {
    ExpressionTree argument = tree.getArguments().get(1);
    if (isSubtype(ASTHelpers.getType(argument), FORMATTABLE_TYPE, state)) {
      /*
       * `Formattable` arguments are handled specially; `String#valueOf` is not a suitable
       * alternative.
       */
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage("Prefer `String#valueOf` over `String#format`")
        .addFix(SuggestedFix.replace(tree, withStringConversionExpression(argument, state)))
        .build();
  }

  /**
   * Unless the given {@code String.format} expression includes {@link Formattable} arguments,
   * suggests replacing it with a {@code String.join} expression using the specified argument
   * separator.
   */
  private Description trySuggestExplicitJoin(
      MethodInvocationTree tree, String separator, VisitorState state) {
    Iterator<? extends ExpressionTree> arguments = tree.getArguments().iterator();

    SuggestedFix.Builder fix =
        SuggestedFix.builder()
            .replace(tree.getMethodSelect(), "String.join")
            .replace(arguments.next(), Constants.format(separator));

    while (arguments.hasNext()) {
      ExpressionTree argument = arguments.next();
      Type argumentType = ASTHelpers.getType(argument);
      if (isSubtype(argumentType, FORMATTABLE_TYPE, state)) {
        /*
         * `Formattable` arguments are handled specially; `String#join` is not a suitable
         * alternative.
         */
        return Description.NO_MATCH;
      }

      if (!isSubtype(argumentType, CHAR_SEQUENCE_TYPE, state)) {
        /*
         * The argument was previously implicitly converted to a string; now this must happen
         * explicitly.
         */
        fix.replace(argument, withStringConversionExpression(argument, state));
      }
    }

    return describeMatch(tree, fix.build());
  }

  private static boolean isSubtype(
      @Nullable Type subType, Supplier<Type> superType, VisitorState state) {
    return ASTHelpers.isSubtype(subType, superType.get(state), state);
  }

  private static String withStringConversionExpression(
      ExpressionTree argument, VisitorState state) {
    return String.format("String.valueOf(%s)", SourceCode.treeToString(argument, state));
  }
}
