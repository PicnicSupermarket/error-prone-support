package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import org.junit.jupiter.api.Test;

final class MoreMatchersTest {
  @Test
  void hasMetaAnnotation() {
    CompilationTestHelper.newInstance(TestMatcher.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.lang.annotation.Inherited;",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.RepeatedTest;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.TestTemplate;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class A {",
            "  void negative1() {}",
            "",
            "  @Test",
            "  void negative2() {}",
            "",
            "  @AfterAll",
            "  void negative3() {}",
            "",
            "  @TestTemplate",
            "  void negative4() {}",
            "",
            "  @InheritedWithoutMetaAnnotation",
            "  void negative5() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest",
            "  void positive1() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @RepeatedTest(2)",
            "  void positive2() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @InheritedWithMetaAnnotation",
            "  void positive3() {}",
            "",
            "  @Inherited",
            "  @interface InheritedWithoutMetaAnnotation {}",
            "",
            "  @Inherited",
            "  @TestTemplate",
            "  @interface InheritedWithMetaAnnotation {}",
            "",
            "  class B extends A {",
            "    @Override",
            "    void negative5() {}",
            "",
            "    @Override",
            "    // BUG: Diagnostic contains:",
            "    void positive3() {}",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that delegates to {@link MoreMatchers#hasMetaAnnotation(String)} . */
  @BugPattern(summary = "Interacts with `MoreMatchers` for testing purposes", severity = ERROR)
  public static final class TestMatcher extends BugChecker
      implements AnnotationTreeMatcher, MethodTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<Tree> DELEGATE =
        MoreMatchers.hasMetaAnnotation("org.junit.jupiter.api.TestTemplate");

    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
