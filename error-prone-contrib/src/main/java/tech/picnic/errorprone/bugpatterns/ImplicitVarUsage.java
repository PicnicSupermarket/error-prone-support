package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static javax.lang.model.element.ElementKind.LOCAL_VARIABLE;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;

/** A {@link BugChecker} that flags usages of {@code var} keyword when the type is not explicit. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Consider using explicit type instead of 'var' to improve code clarity",
    link = BUG_PATTERNS_BASE_URL + "ImplicitVarUsage",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class ImplicitVarUsage extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Creates a new {@link ImplicitVarUsage} instance. */
  public ImplicitVarUsage() {}

  @Override
  public Description matchVariable(VariableTree variableTree, VisitorState visitorState) {
    // Only check local variables with implicit 'var' type
    if (ASTHelpers.getSymbol(variableTree).getKind() != LOCAL_VARIABLE
        || !ASTHelpers.hasImplicitType(variableTree, visitorState)) {
      return NO_MATCH;
    }

    ExpressionTree initializer = variableTree.getInitializer();
    if (initializer == null) {
      return NO_MATCH;
    }

    // Allow literals
    if (initializer.getKind().name().endsWith("_LITERAL")) {
      return NO_MATCH;
    }

    // Allow type casts
    if (initializer instanceof TypeCastTree) {
      return NO_MATCH;
    }

    // Allow constructor calls with 'new'
    if (initializer instanceof NewClassTree) {
      return NO_MATCH;
    }

    // Allow expressions with .class literals
    String initializerSource = visitorState.getSourceForNode(initializer);
    if (initializerSource != null && initializerSource.contains(".class")) {
      return NO_MATCH;
    }

    // Allow constructor references (::new)
    if (initializerSource != null && initializerSource.contains("::new")) {
      return NO_MATCH;
    }

    // Get the actual type for suggestion
    Type type = ASTHelpers.getType(initializer);
    if (type == null) {
      return NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder =
        SuggestedFix.builder()
            .setShortDescription(
                "Consider using explicit type instead of 'var' to improve code clarity");
    String fixType = SuggestedFixes.qualifyType(visitorState, fixBuilder, type);
    String variableName = variableTree.getName().toString();
    String replacement = String.format("%s %s = %s;", fixType, variableName, initializerSource);

    fixBuilder.replace(variableTree, replacement);

    return describeMatch(variableTree, fixBuilder.build());
  }
}
