package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.raw;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.subOf;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;

/** A {@link BugChecker} that flags nesting of {@link Optional Optionals}. */
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
  private static final Supplier<Type> OPTIONAL_OF_OPTIONAL =
      VisitorState.memoize(generic(OPTIONAL, subOf(raw(OPTIONAL))));

  /** Instantiates a new {@link NestedOptionals} instance. */
  public NestedOptionals() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    Type type = OPTIONAL_OF_OPTIONAL.get(state);
    if (type == null || !state.getTypes().isSubtype(ASTHelpers.getType(tree), type)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree);
  }
}
