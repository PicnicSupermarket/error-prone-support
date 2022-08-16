package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.anyMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import javax.annotation.Nullable;

/** A {@link BugChecker} which flags any (embedded) nesting of {@link Optional Optionals}. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid creating nested optionals.",
    linkType = NONE,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class NestingOptionals extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String JAVA_OPTIONAL = "java.util.Optional";
  private static final Matcher<ExpressionTree> ANY_OPTIONAL_METHOD =
      anyMethod().onDescendantOf(JAVA_OPTIONAL);

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ANY_OPTIONAL_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Boolean foundNestedOptional = tree.accept(new NestedOptionalDetector(), state);
    if (foundNestedOptional == null || !foundNestedOptional) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree).build();
  }

  private static class NestedOptionalDetector extends TreeScanner<Boolean, VisitorState> {
    @Nullable
    @Override
    public Boolean visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      if (ASTHelpers.getType(node).getTypeArguments().stream()
          .anyMatch(NestedOptionalDetector::typeIsJavaOptional)) {
        return true;
      }
      return super.visitMethodInvocation(node, state);
    }

    private static boolean typeIsJavaOptional(Type type) {
      return type.asElement().getQualifiedName().contentEquals(JAVA_OPTIONAL);
    }
  }
}
