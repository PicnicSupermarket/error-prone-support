package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LiteralTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.util.Constants;
import java.util.regex.Pattern;
import tech.picnic.errorprone.bugpatterns.util.ThirdPartyLibrary;

/**
 * A {@link BugChecker} that flags literal strings in Error Prone Support code that represent the
 * fully qualified class name of a type that's on Error Prone's classpath.
 *
 * <p>Deriving such strings from the associated {@link Class} instance makes for more maintainable
 * code.
 */
// XXX: Consider generalizing this to a `RuntimeClasspath` check with a configurable
// `CLASSPATH_TYPES` regex. Such a check would likely be a no-op by default, with Error Prone
// Support's parent specifying the regex that is currently hard-coded.
// XXX: As-is this check may suggest usage of a string literal even if the relevant type will be on
// the runtime classpath. Review how we can further reduce false-positives.
// XXX: Slightly "out there", but we could try to derive the subset of classpath types likely to be
// available at runtime from the `compile`-scoped Maven dependencies in the `pom.xml` file of
// the module being compiled.
// XXX: Right now this check does not rewrite primitive string references such as `"int"` to
// `int.class.getCanonicalName()`. Review whether that's worth it.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Prefer `Class#getCanonicalName()` over an equivalent string literal if and only if the "
            + "type will be on the runtime classpath",
    link = BUG_PATTERNS_BASE_URL + "ErrorProneRuntimeClasspath",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = FRAGILE_CODE)
public final class ErrorProneRuntimeClasspath extends BugChecker
    implements LiteralTreeMatcher, MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> GET_CANONICAL_NAME_INVOCATION =
      instanceMethod().onExactClass(Class.class.getCanonicalName()).named("getCanonicalName");
  private static final ImmutableSet<String> PRIMITIVE_TYPES =
      Primitives.allPrimitiveTypes().stream()
          .map(Class::getCanonicalName)
          .collect(toImmutableSet());

  /**
   * A pattern that matches fully qualified type names that are expected to be runtime dependencies
   * of Error Prone, and that are thus presumed to be unconditionally present on a bug checker's
   * runtime classpath.
   */
  private static final Pattern CLASSPATH_TYPES =
      Pattern.compile(
          "com\\.google\\.common\\..*|com\\.google\\.errorprone\\.([^.]+(?<!TestHelper)(\\..*)?)|java\\..*");

  /** Instantiates a new {@link ErrorProneRuntimeClasspath} instance. */
  public ErrorProneRuntimeClasspath() {}

  @Override
  public Description matchLiteral(LiteralTree tree, VisitorState state) {
    String value = ASTHelpers.constValue(tree, String.class);
    if (value == null
        || !CLASSPATH_TYPES.matcher(value).matches()
        || state.findEnclosing(AnnotationTree.class) != null) {
      return Description.NO_MATCH;
    }

    SuggestedFix fix = trySuggestClassReference(tree, value, state);
    if (fix.isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "This type will be on the runtime classpath; use `Class#getCanonicalName()` instead")
        .addFix(fix)
        .build();
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!GET_CANONICAL_NAME_INVOCATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Symbol receiver = ASTHelpers.getSymbol(ASTHelpers.getReceiver(tree));
    if (receiver == null
        || !(receiver.owner instanceof ClassSymbol)
        || PRIMITIVE_TYPES.contains(receiver.owner.getQualifiedName().toString())
        || CLASSPATH_TYPES.matcher(receiver.owner.getQualifiedName()).matches()) {
      return Description.NO_MATCH;
    }

    /*
     * This class reference may not be safe; suggest using a string literal instead. (Note that
     * dropping the type reference may make the associated import statement (if any) obsolete.
     * Dropping such imports is left to Error Prone's `RemoveUnusedImports` check.)
     */
    return buildDescription(tree)
        .setMessage("This type may not be on the runtime classpath; use a string literal instead")
        .addFix(
            SuggestedFix.replace(
                tree, Constants.format(receiver.owner.getQualifiedName().toString())))
        .build();
  }

  private static SuggestedFix trySuggestClassReference(
      LiteralTree tree, String value, VisitorState state) {
    if (isTypeOnClasspath(value, state)) {
      return suggestClassReference(tree, value, "", state);
    }

    int lastDot = value.lastIndexOf('.');
    String type = value.substring(0, lastDot);
    if (isTypeOnClasspath(type, state)) {
      return suggestClassReference(tree, type, value.substring(lastDot), state);
    }

    return SuggestedFix.emptyFix();
  }

  private static SuggestedFix suggestClassReference(
      LiteralTree original, String type, String suffix, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    String identifier = SuggestedFixes.qualifyType(state, fix, type);
    return fix.replace(
            original,
            identifier
                + ".class.getCanonicalName()"
                + (suffix.isEmpty() ? "" : (" + " + Constants.format(suffix))))
        .build();
  }

  private static boolean isTypeOnClasspath(String type, VisitorState state) {
    try {
      return ThirdPartyLibrary.canIntroduceUsage(type, state);
    } catch (
        @SuppressWarnings("java:S1166" /* Not exceptional. */)
        IllegalArgumentException e) {
      return false;
    }
  }
}
