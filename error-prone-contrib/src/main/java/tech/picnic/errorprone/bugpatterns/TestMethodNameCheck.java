package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.fixes.SuggestedFixes.*;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.util.Name;
import java.util.Optional;

/** A {@link BugChecker} which flags redundant {@code @Test} annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "TestMethodNameCheck",
    summary = "Method names starting with a 'test' prefix can likely be removed",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.WARNING,
    tags = BugPattern.StandardTags.SIMPLIFICATION)
public final class TestMethodNameCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String TEST_PREFIX = "test";

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    MethodSymbol sym = ASTHelpers.getSymbol(tree);
    if (sym == null
        || !ASTHelpers.isJUnitTestCode(state)
        || !hasTestPrefix(sym.getQualifiedName())) {
      return Description.NO_MATCH;
    }

    String originalMethodName = sym.getQualifiedName().toString();
    Optional<String> newMethodName = removeTestPrefix(originalMethodName);
    if (newMethodName.isEmpty()) {
      /* The new method name is not valid */
      return Description.NO_MATCH;
    } else {
      return describeMatch(tree, renameMethod(tree, newMethodName.orElseThrow(), state));
    }
  }

  /** Determines whether the provided method name starts with the word 'test'. */
  private static boolean hasTestPrefix(Name methodName) {
    return methodName.length() > 4 && methodName.toString().startsWith(TEST_PREFIX);
  }

  /** Removes the 'test' prefix, if possible. */
  private static Optional<String> removeTestPrefix(String methodName) {
    StringBuilder sb = new StringBuilder(methodName);
    sb.delete(0, 4);
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    if (Character.isDigit(sb.charAt(0))) {
      return Optional.empty();
    }
    return Optional.of(sb.toString());
  }
}
