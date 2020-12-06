package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.Matchers.isType;

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
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.AnnotationValue;

/** A {@link BugChecker} which flags ambiguous {@code @JsonCreator}s in enums. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AmbiguousJsonCreator",
    summary = "JsonCreator.Mode should be set for single-argument creators",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.LIKELY_ERROR,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class AmbiguousJsonCreatorCheck extends BugChecker implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> JSON_CREATOR_ANNOTATION =
      isType("com.fasterxml.jackson.annotation.JsonCreator");

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    if (!JSON_CREATOR_ANNOTATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ClassTree clazz = state.findEnclosing(ClassTree.class);
    if (clazz == null || clazz.getKind() != Tree.Kind.ENUM) {
      return Description.NO_MATCH;
    }

    MethodTree method = state.findEnclosing(MethodTree.class);
    if (method == null || method.getParameters().size() != 1) {
      return Description.NO_MATCH;
    }

    Optional<Symbol.VarSymbol> mode =
        ASTHelpers.getAnnotationMirror(tree).getElementValues().entrySet().stream()
            .filter(entry -> entry.getKey().getSimpleName().contentEquals("mode"))
            .map(Map.Entry::getValue)
            .map(AnnotationValue::getValue)
            .filter(Symbol.VarSymbol.class::isInstance)
            .map(Symbol.VarSymbol.class::cast)
            .filter(varSymbol -> !varSymbol.getSimpleName().contentEquals("DEFAULT"))
            .findFirst();

    if (mode.isPresent()) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree, SuggestedFix.replace(tree, "@JsonCreator(mode = JsonCreator.Mode.DELEGATING)"));
  }
}
