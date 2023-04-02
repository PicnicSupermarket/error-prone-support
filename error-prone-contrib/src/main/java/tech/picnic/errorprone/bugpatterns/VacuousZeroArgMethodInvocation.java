package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags method invocations without arguments, in cases where such
 * invocation amounts to a no-op.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid no-op invocations of varargs methods without arguments",
    link = BUG_PATTERNS_BASE_URL + "VacuousZeroArgMethodInvocation",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class VacuousZeroArgMethodInvocation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLAGGED_INSTANCE_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf(ImmutableCollection.Builder.class.getCanonicalName())
              .named("add"),
          instanceMethod()
              .onDescendantOfAny(
                  "com.google.errorprone.BugCheckerRefactoringTestHelper",
                  "com.google.errorprone.CompilationTestHelper")
              .named("setArgs"));
  private static final Matcher<ExpressionTree> FLAGGED_STATIC_METHOD =
      staticMethod().onClass(Refaster.class.getCanonicalName()).named("anyOf");

  /** Instantiates a new {@link VacuousZeroArgMethodInvocation} instance. */
  public VacuousZeroArgMethodInvocation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!tree.getArguments().isEmpty()) {
      return Description.NO_MATCH;
    }

    if (FLAGGED_INSTANCE_METHOD.matches(tree, state)) {
      ExpressionTree receiver = ASTHelpers.getReceiver(tree);
      if (receiver == null) {
        // XXX: Test this using an `ImmutableCollection.Builder` subtype in which we call `add()`.
        // XXX: This call can be removed completely, unless the result is used in some way (by being
        // dereferenced, or passed as an argument to another method).
        return describeMatch(tree);
      }

      // XXX: This logic is also used in `NonEmptyMono`; worthy of a `SourceCode` utility method?
      return describeMatch(
          tree, SuggestedFix.replace(tree, SourceCode.treeToString(receiver, state)));
    }

    if (FLAGGED_STATIC_METHOD.matches(tree, state)) {
      // XXX: Drop the method invocation if its result is not used in some way (by being
      // dereferenced, or passed as an argument to another method).
      return describeMatch(tree);
    }

    return Description.NO_MATCH;
  }
}
