package tech.picnic.errorprone.bugpatterns;

import static tech.picnic.errorprone.bugpatterns.Util.treeToString;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.util.Name;
import java.util.Optional;

/** A {@link BugChecker} which flags redundant {@code @Test} annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "TestAnnotation",
    summary = "Method names starting with a 'test' prefix can likely be removed",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.WARNING,
    tags = BugPattern.StandardTags.SIMPLIFICATION)
public final class TestMethodNameCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String PARAMETERIZED_TEST_ANNOTATION =
      "org.junit.jupiter.params.ParameterizedTest";
  private static final String TEST_ANNOTATION = "org.junit.jupiter.api.Test";
  private static final String TEST_PREFIX = "test";

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (tree.getBody() == null || !tree.getBody().getStatements().isEmpty()) {
      return Description.NO_MATCH;
    }

    MethodSymbol sym = ASTHelpers.getSymbol(tree);
    if (sym == null
        || !(ASTHelpers.hasAnnotation(sym, TEST_ANNOTATION, state)
            || ASTHelpers.hasAnnotation(sym, PARAMETERIZED_TEST_ANNOTATION, state))
        || !hasTestPrefix(sym.getQualifiedName())) {
      return Description.NO_MATCH;
    }

    String originalMethodName = sym.getQualifiedName().toString();
    Optional<String> newMethodName = removeTestPrefix(originalMethodName);

    if (newMethodName.isEmpty()) {
      return Description.NO_MATCH;
    } else {
      return describeMatch(
          tree,
          SuggestedFix.replace(
              tree,
              treeToString(tree, state).replace(originalMethodName, newMethodName.orElseThrow())));
    }
  }

  /** Determines whether the provided method name starts with the word 'test'. */
  private static boolean hasTestPrefix(Name methodName) {
    return methodName.length() > 4 && methodName.toString().startsWith(TEST_PREFIX);
  }

  /** Removes the 'test' prefix, if possible. */
  private static Optional<String> removeTestPrefix(String methodName) {
    char[] c = methodName.substring(4).toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    if (Character.isDigit(c[0])) {
      return Optional.empty();
    }

    return Optional.of(new String(c));
  }
}
