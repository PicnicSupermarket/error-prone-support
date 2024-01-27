package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * A {@link BugChecker} that suggests a canonical set of modifiers for Refaster class and method
 * definitions.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Refaster class and method definitions should specify a canonical set of modifiers",
    link = BUG_PATTERNS_BASE_URL + "RefasterRuleModifiers",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class RefasterRuleModifiers extends BugChecker
    implements ClassTreeMatcher, MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> AFTER_TEMPLATE_METHOD = hasAnnotation(AfterTemplate.class);
  private static final Matcher<Tree> PLACEHOLDER_METHOD = hasAnnotation(Placeholder.class);
  private static final Matcher<Tree> REFASTER_METHOD =
      anyOf(BEFORE_TEMPLATE_METHOD, AFTER_TEMPLATE_METHOD, PLACEHOLDER_METHOD);

  /** Instantiates a new {@link RefasterRuleModifiers} instance. */
  public RefasterRuleModifiers() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!hasMatchingMember(tree, BEFORE_TEMPLATE_METHOD, state)) {
      /* This class does not contain a Refaster template. */
      return Description.NO_MATCH;
    }

    SuggestedFix fix = suggestCanonicalModifiers(tree, state);
    return fix.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fix);
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!REFASTER_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return SuggestedFixes.removeModifiers(
            tree,
            state,
            Modifier.FINAL,
            Modifier.PRIVATE,
            Modifier.PROTECTED,
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.SYNCHRONIZED)
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static SuggestedFix suggestCanonicalModifiers(ClassTree tree, VisitorState state) {
    Set<Modifier> modifiersToAdd = EnumSet.noneOf(Modifier.class);
    Set<Modifier> modifiersToRemove =
        EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC, Modifier.SYNCHRONIZED);

    if (!hasMatchingMember(tree, PLACEHOLDER_METHOD, state)) {
      /*
       * Rules without a `@Placeholder` method should be `final`. Note that Refaster enforces
       * that `@Placeholder` methods are `abstract`, so rules _with_ such a method will
       * naturally be `abstract` and non-`final`.
       */
      modifiersToAdd.add(Modifier.FINAL);
      modifiersToRemove.add(Modifier.ABSTRACT);
    }

    if (ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class) != null) {
      /* Nested classes should be `static`. */
      modifiersToAdd.add(Modifier.STATIC);
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    SuggestedFixes.addModifiers(tree, tree.getModifiers(), state, modifiersToAdd)
        .ifPresent(fix::merge);
    SuggestedFixes.removeModifiers(tree.getModifiers(), state, modifiersToRemove)
        .ifPresent(fix::merge);
    return fix.build();
  }

  private static boolean hasMatchingMember(
      ClassTree tree, Matcher<Tree> matcher, VisitorState state) {
    return tree.getMembers().stream().anyMatch(member -> matcher.matches(member, state));
  }
}
