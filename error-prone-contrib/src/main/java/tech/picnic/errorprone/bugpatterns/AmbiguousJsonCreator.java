package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol;
import java.util.Map;
import javax.lang.model.element.AnnotationValue;

/** A {@link BugChecker} that flags ambiguous {@code @JsonCreator}s in enums. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "`JsonCreator.Mode` should be set for single-argument creators",
    link = BUG_PATTERNS_BASE_URL + "AmbiguousJsonCreator",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class AmbiguousJsonCreator extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> IS_JSON_CREATOR_ANNOTATION =
      isType("com.fasterxml.jackson.annotation.JsonCreator");

  /** Instantiates a new {@link AmbiguousJsonCreator} instance. */
  public AmbiguousJsonCreator() {}

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    if (!IS_JSON_CREATOR_ANNOTATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ClassTree clazz = state.findEnclosing(ClassTree.class);
    if (clazz == null || clazz.getKind() != Kind.ENUM) {
      return Description.NO_MATCH;
    }

    MethodTree method = state.findEnclosing(MethodTree.class);
    if (method == null || method.getParameters().size() != 1) {
      return Description.NO_MATCH;
    }

    boolean customMode =
        ASTHelpers.getAnnotationMirror(tree).getElementValues().entrySet().stream()
            .filter(entry -> entry.getKey().getSimpleName().contentEquals("mode"))
            .map(Map.Entry::getValue)
            .map(AnnotationValue::getValue)
            .filter(Symbol.VarSymbol.class::isInstance)
            .map(Symbol.VarSymbol.class::cast)
            .anyMatch(varSymbol -> !varSymbol.getSimpleName().contentEquals("DEFAULT"));

    return customMode
        ? Description.NO_MATCH
        : describeMatch(
            tree, SuggestedFix.replace(tree, "@JsonCreator(mode = JsonCreator.Mode.DELEGATING)"));
  }
}
