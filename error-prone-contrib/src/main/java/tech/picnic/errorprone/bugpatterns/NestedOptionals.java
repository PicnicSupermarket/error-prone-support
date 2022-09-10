package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.util.NestedTypes;

/** A {@link BugChecker} which flags nesting of {@link Optional Optionals}. */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid nesting `Optional`s inside `Optional`s; the resultant code is hard to reason about",
    linkType = NONE,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class NestedOptionals extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> OPTIONAL = Suppliers.typeFromClass(Optional.class);

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return NestedTypes.isSameTypeNested(OPTIONAL, tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }
}
