package com.picnicinternational.errorprone.bugpatterns;

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
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

@AutoService(BugChecker.class)
@BugPattern(
  name = "AutowiredConstructor",
  summary = "Omit `@Autowired` on a class' sole constuctor, as it is redundant",
  linkType = LinkType.NONE,
  severity = SeverityLevel.SUGGESTION,
  tags = StandardTags.STYLE,
  providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION
)
public final class AutowiredConstructorCheck extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> AUTOWIRED_ANNOTATION =
      new AnnotationType("org.springframework.beans.factory.annotation.Autowired");

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    if (!AUTOWIRED_ANNOTATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (!withinConstructor(state)) {
      return Description.NO_MATCH;
    }

    if (ASTHelpers.getConstructors(state.findEnclosing(ClassTree.class)).size() != 1) {
      return Description.NO_MATCH;
    }

    // XXX: We could remove the `@Autowired` import if no other usages remain.
    return describeMatch(tree, SuggestedFix.delete(tree));
  }

  private static boolean withinConstructor(VisitorState state) {
    MethodTree method = state.findEnclosing(MethodTree.class);
    if (method == null) {
      return false;
    }

    Symbol sym = ASTHelpers.getSymbol(method);
    return sym instanceof MethodSymbol && ((MethodSymbol) sym).isConstructor();
  }
}
