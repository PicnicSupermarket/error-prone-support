package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasMethod;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.not;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.TEST_METHOD;
import static tech.picnic.errorprone.utils.MoreMatchers.hasMetaAnnotation;

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
import com.sun.source.tree.ClassTree;
import javax.lang.model.element.Modifier;

/**
 * A {@link BugChecker} that flags non-final and non package-private JUnit test class declarations,
 * unless abstract.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Non-abstract JUnit test classes should be declared package-private and final",
    linkType = CUSTOM,
    link = BUG_PATTERNS_BASE_URL + "JUnitClassModifiers",
    severity = SUGGESTION,
    tags = STYLE)
public final class JUnitClassModifiers extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ClassTree> HAS_SPRING_CONFIGURATION_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.springframework.context.annotation.Configuration"),
              hasMetaAnnotation("org.springframework.context.annotation.Configuration")));
  private static final Matcher<ClassTree> TEST_CLASS_WITH_INCORRECT_MODIFIERS =
      allOf(
          hasMethod(TEST_METHOD),
          not(hasModifier(Modifier.ABSTRACT)),
          anyOf(
              hasModifier(Modifier.PRIVATE),
              hasModifier(Modifier.PROTECTED),
              hasModifier(Modifier.PUBLIC),
              allOf(not(hasModifier(Modifier.FINAL)), not(HAS_SPRING_CONFIGURATION_ANNOTATION))));

  /** Instantiates a new {@link JUnitClassModifiers} instance. */
  public JUnitClassModifiers() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!TEST_CLASS_WITH_INCORRECT_MODIFIERS.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    SuggestedFixes.removeModifiers(
            tree.getModifiers(),
            state,
            ImmutableSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC))
        .ifPresent(fixBuilder::merge);

    if (!HAS_SPRING_CONFIGURATION_ANNOTATION.matches(tree, state)) {
      SuggestedFixes.addModifiers(tree, state, Modifier.FINAL).ifPresent(fixBuilder::merge);
    }

    return describeMatch(tree, fixBuilder.build());
  }
}
