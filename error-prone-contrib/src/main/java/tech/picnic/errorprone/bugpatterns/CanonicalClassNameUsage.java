package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anything;
import static com.google.errorprone.matchers.Matchers.classLiteral;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.receiverOfInvocation;
import static com.google.errorprone.matchers.Matchers.toType;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import java.util.regex.Pattern;

/**
 * A {@link BugChecker} that flags invocations of {@link Class#getName()} where {@link
 * Class#getCanonicalName()} was likely meant.
 *
 * <p>For top-level types these two methods generally return the same result, but for nested types
 * the former separates identifiers using a dollar sign ({@code $}) rather than a dot ({@code .}).
 *
 * @implNote This check currently only flags {@link Class#getName()} invocations on class literals,
 *     and doesn't flag method references. This avoids false positives, such as suggesting use of
 *     {@link Class#getCanonicalName()} in contexts where the canonical name is {@code null}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "This code should likely use the type's canonical name",
    link = BUG_PATTERNS_BASE_URL + "CanonicalClassNameUsage",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class CanonicalClassNameUsage extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> GET_NAME_INVOCATION =
      toType(
          MethodInvocationTree.class,
          allOf(
              receiverOfInvocation(classLiteral(anything())),
              instanceMethod().onExactClass(Class.class.getCanonicalName()).named("getName")));
  private static final Pattern CANONICAL_NAME_USING_TYPES =
      Pattern.compile("(com\\.google\\.errorprone|tech\\.picnic\\.errorprone)\\..*");

  /** Instantiates a new {@link CanonicalClassNameUsage} instance. */
  public CanonicalClassNameUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!GET_NAME_INVOCATION.matches(tree, state) || !isPassedToCanonicalNameUsingType(state)) {
      /*
       * This is not a `class.getName()` invocation of which the result is passed to another method
       * known to accept canonical type names.
       */
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree, SuggestedFixes.renameMethodInvocation(tree, "getCanonicalName", state));
  }

  private static boolean isPassedToCanonicalNameUsingType(VisitorState state) {
    @Var TreePath path = state.getPath().getParentPath();
    while (path.getLeaf() instanceof BinaryTree) {
      path = path.getParentPath();
    }

    return path.getLeaf() instanceof MethodInvocationTree methodInvocation
        && isOwnedByCanonicalNameUsingType(ASTHelpers.getSymbol(methodInvocation));
  }

  private static boolean isOwnedByCanonicalNameUsingType(MethodSymbol symbol) {
    return CANONICAL_NAME_USING_TYPES.matcher(symbol.owner.getQualifiedName()).matches();
  }
}
