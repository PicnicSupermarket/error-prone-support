package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyMethod;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.anything;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.isNonNullUsingDataflow;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.BinaryTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.CompoundAssignmentTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.Console;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;
import tech.picnic.errorprone.bugpatterns.util.Flags;
import tech.picnic.errorprone.bugpatterns.util.MethodMatcherFactory;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that flags redundant explicit string conversions. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid redundant string conversions when possible",
    link = BUG_PATTERNS_BASE_URL + "RedundantStringConversion",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
@SuppressWarnings({
  "java:S1192" /* Factoring out repeated method names impacts readability. */,
  "java:S2160" /* Super class equality definition suffices. */,
  "key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
})
public final class RedundantStringConversion extends BugChecker
    implements BinaryTreeMatcher, CompoundAssignmentTreeMatcher, MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String EXTRA_STRING_CONVERSION_METHODS_FLAG =
      "RedundantStringConversion:ExtraConversionMethods";

  private static final Matcher<ExpressionTree> ANY_EXPR = anything();
  private static final Matcher<ExpressionTree> LOCALE = isSameType(Locale.class);
  private static final Matcher<ExpressionTree> MARKER = isSubtypeOf("org.slf4j.Marker");
  private static final Matcher<ExpressionTree> STRING = isSameType(String.class);
  private static final Matcher<ExpressionTree> THROWABLE = isSubtypeOf(Throwable.class);
  private static final Matcher<ExpressionTree> NON_NULL_STRING =
      allOf(STRING, isNonNullUsingDataflow());
  private static final Matcher<ExpressionTree> NOT_FORMATTABLE =
      not(isSubtypeOf(Formattable.class));
  private static final Matcher<MethodInvocationTree> WELL_KNOWN_STRING_CONVERSION_METHODS =
      anyOf(
          instanceMethod()
              .onDescendantOfAny(Object.class.getCanonicalName())
              .named("toString")
              .withNoParameters(),
          allOf(
              argumentCount(1),
              anyOf(
                  staticMethod()
                      .onClassAny(
                          Stream.concat(
                                  Primitives.allWrapperTypes().stream(), Stream.of(Objects.class))
                              .map(Class::getName)
                              .collect(toImmutableSet()))
                      .named("toString"),
                  allOf(
                      staticMethod().onClass(String.class.getCanonicalName()).named("valueOf"),
                      not(
                          anyMethod()
                              .anyClass()
                              .withAnyName()
                              .withParametersOfType(
                                  ImmutableList.of(Suppliers.arrayOf(Suppliers.CHAR_TYPE))))))));
  private static final Matcher<ExpressionTree> STRINGBUILDER_APPEND_INVOCATION =
      instanceMethod()
          .onDescendantOf(StringBuilder.class.getCanonicalName())
          .named("append")
          .withParameters(String.class.getCanonicalName());
  private static final Matcher<ExpressionTree> STRINGBUILDER_INSERT_INVOCATION =
      instanceMethod()
          .onDescendantOf(StringBuilder.class.getCanonicalName())
          .named("insert")
          .withParameters(int.class.getCanonicalName(), String.class.getCanonicalName());
  private static final Matcher<ExpressionTree> FORMATTER_INVOCATION =
      anyOf(
          staticMethod().onClass(String.class.getCanonicalName()).named("format"),
          instanceMethod().onDescendantOf(Formatter.class.getCanonicalName()).named("format"),
          instanceMethod()
              .onDescendantOfAny(
                  PrintStream.class.getCanonicalName(), PrintWriter.class.getCanonicalName())
              .namedAnyOf("format", "printf"),
          instanceMethod()
              .onDescendantOfAny(
                  PrintStream.class.getCanonicalName(), PrintWriter.class.getCanonicalName())
              .namedAnyOf("print", "println")
              .withParameters(Object.class.getCanonicalName()),
          staticMethod()
              .onClass(Console.class.getCanonicalName())
              .namedAnyOf("format", "printf", "readline", "readPassword"));
  private static final Matcher<ExpressionTree> GUAVA_GUARD_INVOCATION =
      anyOf(
          staticMethod()
              .onClass("com.google.common.base.Preconditions")
              .namedAnyOf("checkArgument", "checkState", "checkNotNull"),
          staticMethod()
              .onClass("com.google.common.base.Verify")
              .namedAnyOf("verify", "verifyNotNull"));
  private static final Matcher<ExpressionTree> SLF4J_LOGGER_INVOCATION =
      instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .namedAnyOf("trace", "debug", "info", "warn", "error");

  private final Matcher<MethodInvocationTree> conversionMethodMatcher;

  /** Instantiates a default {@link RedundantStringConversion} instance. */
  public RedundantStringConversion() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link RedundantStringConversion}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  RedundantStringConversion(ErrorProneFlags flags) {
    conversionMethodMatcher = createConversionMethodMatcher(flags);
  }

  @Override
  public Description matchBinary(BinaryTree tree, VisitorState state) {
    if (tree.getKind() != Kind.PLUS) {
      return Description.NO_MATCH;
    }

    ExpressionTree lhs = tree.getLeftOperand();
    ExpressionTree rhs = tree.getRightOperand();
    if (!STRING.matches(lhs, state)) {
      return createDescription(tree, tryFix(rhs, state, STRING));
    }

    List<SuggestedFix.Builder> fixes = new ArrayList<>();

    // XXX: Avoid trying to simplify the RHS twice.
    ExpressionTree preferredRhs = trySimplify(rhs, state).orElse(rhs);
    if (STRING.matches(preferredRhs, state)) {
      tryFix(lhs, state, ANY_EXPR).ifPresent(fixes::add);
    } else {
      tryFix(lhs, state, STRING).ifPresent(fixes::add);
    }
    tryFix(rhs, state, ANY_EXPR).ifPresent(fixes::add);

    return createDescription(tree, fixes.stream().reduce(SuggestedFix.Builder::merge));
  }

  @Override
  public Description matchCompoundAssignment(CompoundAssignmentTree tree, VisitorState state) {
    if (tree.getKind() != Kind.PLUS_ASSIGNMENT || !STRING.matches(tree.getVariable(), state)) {
      return Description.NO_MATCH;
    }

    return createDescription(tree, tryFix(tree.getExpression(), state, ANY_EXPR));
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (STRINGBUILDER_APPEND_INVOCATION.matches(tree, state)) {
      return createDescription(tree, tryFixPositionalConverter(tree.getArguments(), state, 0));
    }

    if (STRINGBUILDER_INSERT_INVOCATION.matches(tree, state)) {
      return createDescription(tree, tryFixPositionalConverter(tree.getArguments(), state, 1));
    }

    if (FORMATTER_INVOCATION.matches(tree, state)) {
      return createDescription(tree, tryFixFormatter(tree.getArguments(), state));
    }

    if (GUAVA_GUARD_INVOCATION.matches(tree, state)) {
      return createDescription(tree, tryFixGuavaGuard(tree.getArguments(), state));
    }

    if (SLF4J_LOGGER_INVOCATION.matches(tree, state)) {
      return createDescription(tree, tryFixSlf4jLogger(tree.getArguments(), state));
    }

    if (instanceMethod().matches(tree, state)) {
      return createDescription(tree, tryFix(tree, state, STRING));
    }

    return createDescription(tree, tryFix(tree, state, NON_NULL_STRING));
  }

  private Optional<SuggestedFix.Builder> tryFixPositionalConverter(
      List<? extends ExpressionTree> arguments, VisitorState state, int index) {
    return Optional.of(arguments)
        .filter(args -> args.size() > index)
        .flatMap(args -> tryFix(args.get(index), state, ANY_EXPR));
  }

  // XXX: Write another check that checks that Formatter patterns don't use `{}` and have a
  // matching number of arguments of the appropriate type. Also flag explicit conversions from
  // `Formattable` to string.
  private Optional<SuggestedFix.Builder> tryFixFormatter(
      List<? extends ExpressionTree> arguments, VisitorState state) {
    /*
     * Formatter methods have an optional first `Locale` parameter; if present, it must be
     * ignored. Arguments after the format string are simplified if they are of type `String`,
     * _unless_ the simplification results in an expression of type `java.util.Formattable`,
     * because the `%s` format specifier treats values of this type specially. (And so dropping
     * the string conversion would change behavior.) Note that dropping the string conversion
     * should not otherwise have any effect: if the original string argument was valid for the
     * provided format string, then the associated format specifier must have been `%s`, which
     * performs simple string conversion for all other types, including all primitive types.
     */
    // XXX: since we don't know the runtime type of the arguments, it may be that arguments which
    // *do* implement `java.util.Formattable` are not recognized as such. We could make the check
    // more conservative, but `Formattable` is rarely used... consider at least flagging this
    // caveat.
    return tryFixFormatterArguments(arguments, state, LOCALE, NOT_FORMATTABLE);
  }

  private Optional<SuggestedFix.Builder> tryFixGuavaGuard(
      List<? extends ExpressionTree> arguments, VisitorState state) {
    /*
     * All Guava guard methods accept a value to be checked, a format string and zero or more
     * value to be plugged into said format string.
     */
    return tryFixFormatterArguments(arguments, state, ANY_EXPR, ANY_EXPR);
  }

  // XXX: Write another check that checks that SLF4J patterns don't use `%s` and have a matching
  // number of arguments of the appropriate type. Also flag explicit conversions from `Throwable` to
  // string as the last logger argument. Suggests either dropping the conversion or going with
  // `Throwable#getMessage()` instead.
  private Optional<SuggestedFix.Builder> tryFixSlf4jLogger(
      List<? extends ExpressionTree> arguments, VisitorState state) {
    /*
     * SLF4J treats the final argument to a log statement specially if it is a `Throwable`: it
     * will always choose to render the associated stacktrace, even if the argument has a
     * matching `{}` placeholder. (In this case the `{}` will simply be logged verbatim.) So if
     * a log statement's final argument is the string representation of a `Throwable`, then we
     * must not strip this explicit string conversion, as that would change the statement's
     * semantics.
     */
    // XXX: Not so nice: we effectively try to simplify the final argument twice.
    boolean omitLast =
        !arguments.isEmpty()
            && trySimplify(arguments.get(arguments.size() - 1), state)
                .filter(replacement -> THROWABLE.matches(replacement, state))
                .isPresent();
    return tryFixFormatterArguments(
        omitLast ? arguments.subList(0, arguments.size() - 1) : arguments, state, MARKER, ANY_EXPR);
  }

  private Optional<SuggestedFix.Builder> tryFixFormatterArguments(
      List<? extends ExpressionTree> arguments,
      VisitorState state,
      Matcher<ExpressionTree> firstArgFilter,
      Matcher<ExpressionTree> remainingArgFilter) {
    if (arguments.isEmpty()) {
      /* This format method accepts no arguments. Some odd overload? */
      return Optional.empty();
    }

    int patternIndex = firstArgFilter.matches(arguments.get(0), state) ? 1 : 0;
    if (arguments.size() <= patternIndex) {
      /* This format method accepts only an ignored parameter. Some odd overload? */
      return Optional.empty();
    }

    /* Simplify the values to be plugged into the format pattern, if possible. */
    return arguments.stream()
        .skip(patternIndex + 1L)
        .map(arg -> tryFix(arg, state, remainingArgFilter))
        .flatMap(Optional::stream)
        .reduce(SuggestedFix.Builder::merge);
  }

  private Optional<SuggestedFix.Builder> tryFix(
      ExpressionTree tree, VisitorState state, Matcher<ExpressionTree> filter) {
    return trySimplify(tree, state, filter)
        .map(
            replacement ->
                SuggestedFix.builder().replace(tree, SourceCode.treeToString(replacement, state)));
  }

  private Optional<ExpressionTree> trySimplify(
      ExpressionTree tree, VisitorState state, Matcher<ExpressionTree> filter) {
    return trySimplify(tree, state)
        .filter(result -> filter.matches(result, state))
        .map(result -> trySimplify(result, state, filter).orElse(result));
  }

  private Optional<ExpressionTree> trySimplify(ExpressionTree tree, VisitorState state) {
    if (tree.getKind() != Kind.METHOD_INVOCATION) {
      return Optional.empty();
    }

    MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;
    if (!conversionMethodMatcher.matches(methodInvocation, state)) {
      return Optional.empty();
    }

    switch (methodInvocation.getArguments().size()) {
      case 0:
        return trySimplifyNullaryMethod(methodInvocation, state);
      case 1:
        return trySimplifyUnaryMethod(methodInvocation, state);
      default:
        throw new IllegalStateException(
            "Cannot simplify method call with two or more arguments: "
                + SourceCode.treeToString(tree, state));
    }
  }

  private static Optional<ExpressionTree> trySimplifyNullaryMethod(
      MethodInvocationTree methodInvocation, VisitorState state) {
    if (!instanceMethod().matches(methodInvocation, state)) {
      return Optional.empty();
    }

    return Optional.of(methodInvocation.getMethodSelect())
        .filter(methodSelect -> methodSelect.getKind() == Kind.MEMBER_SELECT)
        .map(methodSelect -> ((MemberSelectTree) methodSelect).getExpression())
        .filter(expr -> !"super".equals(SourceCode.treeToString(expr, state)));
  }

  private static Optional<ExpressionTree> trySimplifyUnaryMethod(
      MethodInvocationTree methodInvocation, VisitorState state) {
    if (!staticMethod().matches(methodInvocation, state)) {
      return Optional.empty();
    }

    return Optional.of(Iterables.getOnlyElement(methodInvocation.getArguments()));
  }

  private Description createDescription(Tree tree, Optional<SuggestedFix.Builder> fixes) {
    return fixes
        .map(SuggestedFix.Builder::build)
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Matcher<MethodInvocationTree> createConversionMethodMatcher(
      ErrorProneFlags flags) {
    // XXX: ErrorProneFlags#getList splits by comma, but method signatures may also contain commas.
    // For this class methods accepting more than one argument are not valid, but still: not nice.
    return anyOf(
        WELL_KNOWN_STRING_CONVERSION_METHODS,
        new MethodMatcherFactory()
            .create(Flags.getList(flags, EXTRA_STRING_CONVERSION_METHODS_FLAG)));
  }
}
