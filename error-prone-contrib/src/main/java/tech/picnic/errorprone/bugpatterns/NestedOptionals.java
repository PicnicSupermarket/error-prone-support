package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
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

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return isOptionalOfOptional(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
  }

  private static boolean isOptionalOfOptional(Tree tree, VisitorState state) {
    Type optionalType = OPTIONAL.get(state);
    Type type = ASTHelpers.getType(tree);
    if (!ASTHelpers.isSubtype(type, optionalType, state)) {
      return false;
    }

    List<Type> typeArguments = type.getTypeArguments();
    return !typeArguments.isEmpty()
        && ASTHelpers.isSubtype(Iterables.getOnlyElement(typeArguments), optionalType, state);
  }
}
