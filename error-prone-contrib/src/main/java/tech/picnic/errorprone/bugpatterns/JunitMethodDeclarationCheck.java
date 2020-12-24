package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import java.util.Optional;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} which flags improvements in Junit method declarations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "JunitMethodDeclarationCheck",
    summary = "Junit method declaration can likely be improved",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.WARNING,
    tags = BugPattern.StandardTags.SIMPLIFICATION)
public final class JunitMethodDeclarationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String TEST_PREFIX = "test";
  private static final ImmutableSet<Modifier> ILLEGAL_MODIFIERS =
      ImmutableSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
  private static final MultiMatcher<Tree, AnnotationTree> OVERRIDE_ANNOTATION =
      annotations(AT_LEAST_ONE, isType("java.lang.Override"));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    MethodSymbol sym = ASTHelpers.getSymbol(tree);

    if (sym == null
        || !OVERRIDE_ANNOTATION.multiMatchResult(tree, state).matchingNodes().isEmpty()
        || !ASTHelpers.isJUnitTestCode(state)
        || (!hasTestPrefix(sym.getQualifiedName().toString()) && !hasIllegalModifiers(tree))) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder builder = SuggestedFix.builder();
    /* Remove all illegal modifiers, if has. */
    SuggestedFixes.removeModifiers(tree, state, ILLEGAL_MODIFIERS.toArray(Modifier[]::new))
        .ifPresent(builder::merge);
    /* Remove the 'test' prefix, if possible. */
    renameMethod(tree, state, sym.getQualifiedName().toString()).ifPresent(builder::merge);

    return describeMatch(tree, builder.build());
  }

  /** Renames the method removing the 'test' prefix, if possible. */
  private static Optional<SuggestedFix> renameMethod(
      MethodTree tree, VisitorState state, String methodName) {
    if (hasTestPrefix(methodName)) {
      String newMethodName = removeTestPrefix(methodName);

      return isValidMethodName(newMethodName)
          ? Optional.of(SuggestedFixes.renameMethod(tree, newMethodName, state))
          : Optional.empty();
    }

    return Optional.empty();
  }

  /** Removes the 'test' prefix from the method name, if possible. */
  private static String removeTestPrefix(String methodName) {
    StringBuilder sb = new StringBuilder(methodName);
    sb.delete(0, 4);
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));

    return sb.toString();
  }

  /** Determines whether the provided method name is valid. */
  private static boolean isValidMethodName(String methodName) {
    return !methodName.isEmpty() && !Character.isDigit(methodName.charAt(0));
  }

  /** Determines whether the provided method name starts with the word 'test'. */
  private static boolean hasTestPrefix(String methodName) {
    return methodName.length() > 4 && methodName.startsWith(TEST_PREFIX);
  }

  /** Determines whether the method tree has any modifier that is not allowed. */
  private static boolean hasIllegalModifiers(MethodTree tree) {
    return ASTHelpers.getModifiers(tree).getFlags().stream().anyMatch(ILLEGAL_MODIFIERS::contains);
  }
}
