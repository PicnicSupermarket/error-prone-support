package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreMatchers.isSubTypeOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.raw;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.subOf;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;

/** A {@link BugChecker} that flags nesting of {@link Optional Optionals}. */
// XXX: Extend this checker to also flag method return types and variable/field types.
// XXX: Consider generalizing this checker and `NestedPublishers` to a single `NestedMonad` check,
// which e.g. also flags nested `Stream`s. Alternatively, combine these and other checkers into an
// even more generic `ConfusingType` checker.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid nesting `Optional`s inside `Optional`s; the resultant code is hard to reason about",
    link = BUG_PATTERNS_BASE_URL + "NestedOptionals",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class NestedOptionals extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> OPTIONAL = Suppliers.typeFromClass(Optional.class);
  private static final Matcher<Tree> IS_OPTIONAL_OF_OPTIONAL =
      isSubTypeOf(VisitorState.memoize(generic(OPTIONAL, subOf(raw(OPTIONAL)))));

  /** Instantiates a new {@link NestedOptionals} instance. */
  public NestedOptionals() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return IS_OPTIONAL_OF_OPTIONAL.matches(tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }
}
