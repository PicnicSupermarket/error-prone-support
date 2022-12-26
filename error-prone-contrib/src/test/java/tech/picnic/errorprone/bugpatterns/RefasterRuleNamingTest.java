package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class RefasterRuleNamingTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterRuleNaming.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.base.Strings;",
            "import com.google.errorprone.refaster.Refaster;",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class StringRules {",
            "  private StringRules() {}",
            "",
            "  static final class IntegerToStringLastIndexOfChar {",
            "    @BeforeTemplate",
            "    Integer before(String str) {",
            "      return str.lastIndexOf('a');",
            "    }",
            "",
            "    @AfterTemplate",
            "    Integer after(String str) {",
            "      return str.toString().toLowerCase().lastIndexOf('a');",
            "    }",
            "  }",
            "",
            "  static final class IntegerIsEmpty {",
            "    @BeforeTemplate",
            "    boolean before(String str) {",
            "      return Refaster.anyOf(str.length() == 0);",
            "    }",
            "",
            "    @AfterTemplate",
            "    boolean after(String str) {",
            "      return str.isEmpty();",
            "    }",
            "  }",
            "",
            "  static final class IntegerIsNullOrEmpty {",
            "    @BeforeTemplate",
            "    boolean before(String str) {",
            "      return str == null || str.isEmpty();",
            "    }",
            "",
            "    @AfterTemplate",
            "    boolean after(String str) {",
            "      return Strings.isNullOrEmpty(str);",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Disabled
  @Test
  void identificationNegative() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    return str;",
            "  }",
            "",
            "  String nonRefasterMethod(String str) {",
            "    return str;",
            "  }",
            "",
            "  static final class Inner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void stringify() {
    CompilationTestHelper.newInstance(StringifyMethodInvocationTestChecker.class, getClass())
        .addSourceLines(
            "/A.java",
            "import java.util.Collection;",
            "import java.util.List;",
            "import java.util.Map;",
            "import java.util.Optional;",
            "import java.util.Set;",
            "import java.util.Locale;",
            "import com.google.common.collect.ImmutableList;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains: int",
            "   \"a\".lastIndexOf('a');",
            "    // BUG: Diagnostic contains: int-int",
            "   \"a\".lastIndexOf('a', 1);",
            "    // BUG: Diagnostic contains: string",
            "   \"a\".lastIndexOf(\"a\");",
            "    // BUG: Diagnostic contains: string-int",
            "   \"a\".lastIndexOf(\"a\", 1);",
            "",
            "   \"b\".toLowerCase();",
            "   // BUG: Diagnostic contains: locale\",",
            "   \"b\".toLowerCase(Locale.ROOT);",
            "",
            "   ImmutableList.of(1);",
            "   ImmutableList.of(1, 2);",
            "   ImmutableList.of(1, 2, 3);",
            "   ImmutableList.of(1, 2, 3, 4);",
            "   ImmutableList.of(1, 2, 3, 4, 5);",
            "   ImmutableList.of(1, 2, 3, 4, 5, 6);",
            "",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that.... */
  @BugPattern(summary = "Flags invocations of methods with select return types", severity = ERROR)
  public static final class StringifyMethodInvocationTestChecker extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;
    private final RefasterRuleNaming refasterRuleNaming = new RefasterRuleNaming();

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      String result =
          refasterRuleNaming.stringifyParams(
              ImmutableList.copyOf(ASTHelpers.getSymbol(tree).params()));

      return result.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree).setMessage(result).build();
    }
  }
}
