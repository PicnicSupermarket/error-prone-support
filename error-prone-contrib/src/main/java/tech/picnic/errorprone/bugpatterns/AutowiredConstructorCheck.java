package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.AnnotationType;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

/** A {@link BugChecker} which flags redundant {@code @Autowired} constructor annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AutowiredConstructor",
    summary = "Omit `@Autowired` on a class' sole constructor, as it is redundant",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class AutowiredConstructorCheck extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> AUTOWIRED_ANNOTATION =
      new AnnotationType("org.springframework.beans.factory.annotation.Autowired");

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    if (!AUTOWIRED_ANNOTATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (!isWithinConstructor(state)) {
      return Description.NO_MATCH;
    }

    ClassTree clazz = state.findEnclosing(ClassTree.class);
    if (clazz == null || ASTHelpers.getConstructors(clazz).size() != 1) {
      return Description.NO_MATCH;
    }

    /*
     * This is the only `@Autowired` constructor: suggest that it be removed. Note that this likely
     * means that the associated import can be removed as well. Rather than adding code for this case we
     * leave flagging the unused import to Error Prone's `RemoveUnusedImports` check.
     */
    return describeMatch(tree, SuggestedFix.delete(tree));
  }

  private static boolean isWithinConstructor(VisitorState state) {
    MethodTree method = state.findEnclosing(MethodTree.class);
    if (method == null) {
      return false;
    }

    MethodSymbol sym = ASTHelpers.getSymbol(method);
    return sym != null && sym.isConstructor();
  }
}
