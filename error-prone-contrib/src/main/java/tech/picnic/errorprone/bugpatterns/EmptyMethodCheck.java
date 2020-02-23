package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

/** A {@link BugChecker} which flags empty methods that seemingly can simply be deleted. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "EmptyMethod",
    summary = "Empty method can likely be deleted",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.SIMPLIFICATION,
    providesFix = BugPattern.ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class EmptyMethodCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (tree.getBody() == null || !tree.getBody().getStatements().isEmpty()) {
      return Description.NO_MATCH;
    }

    MethodSymbol sym = ASTHelpers.getSymbol(tree);
    if (sym == null
        || ASTHelpers.methodCanBeOverridden(sym)
        || ASTHelpers.hasAnnotation(sym, "java.lang.Override", state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFix.delete(tree));
  }
}
