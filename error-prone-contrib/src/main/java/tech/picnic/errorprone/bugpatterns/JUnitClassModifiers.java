package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasMethod;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.not;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import javax.lang.model.element.Modifier;

/**
 * A {@link BugChecker} that flags non-final and non package-private JUnit test class declarations.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "JUnit test classes should be declared as package private final",
    linkType = CUSTOM,
    link = BUG_PATTERNS_BASE_URL + "JUnitClassDeclaration",
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class JUnitClassModifiers extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<MethodTree, AnnotationTree> TEST_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.Test"),
              hasMetaAnnotation("org.junit.jupiter.api.TestTemplate")));
  private static final Matcher<ClassTree> NON_FINAL_TEST_CLASS =
      allOf(
          not(
              annotations(
                  AT_LEAST_ONE,
                  anyOf(
                      isType("org.springframework.context.annotation.Configuration"),
                      hasMetaAnnotation("org.springframework.context.annotation.Configuration")))),
          hasMethod(TEST_METHOD),
          not(hasModifier(Modifier.ABSTRACT)),
          anyOf(
              not(hasModifier(Modifier.FINAL)),
              hasModifier(Modifier.PRIVATE),
              hasModifier(Modifier.PROTECTED),
              hasModifier(Modifier.PUBLIC)));

  /** Instantiates a new {@link JUnitClassModifiers} instance. */
  public JUnitClassModifiers() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!NON_FINAL_TEST_CLASS.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    SuggestedFixes.addModifiers(tree, state, Modifier.FINAL).ifPresent(fixBuilder::merge);
    SuggestedFixes.removeModifiers(
            tree.getModifiers(),
            state,
            ImmutableSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC))
        .ifPresent(fixBuilder::merge);

    return describeMatch(tree, fixBuilder.build());
  }

  // XXX: Move to a `MoreMatchers` utility class.
  private static Matcher<Tree> hasMetaAnnotation(String annotationClassName) {
    TypePredicate typePredicate = hasAnnotation(annotationClassName);
    return (tree, state) -> {
      Symbol sym = ASTHelpers.getSymbol(tree);
      return sym != null && typePredicate.apply(sym.type, state);
    };
  }

  // XXX: Move to a `MoreTypePredicates` utility class.
  private static TypePredicate hasAnnotation(String annotationClassName) {
    return (type, state) -> ASTHelpers.hasAnnotation(type.tsym, annotationClassName, state);
  }
}
