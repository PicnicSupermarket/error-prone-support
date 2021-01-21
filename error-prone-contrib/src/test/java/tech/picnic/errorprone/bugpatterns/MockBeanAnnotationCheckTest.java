package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class MockBeanAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MockBeanAnnotationCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new MockBeanAnnotationCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "  // BUG: Diagnostic contains:",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  @MockBean private Object object1;",
            "  // BUG: Diagnostic contains:",
            "  @Autowired @MockBean private Object object2;",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "class A {",
            "  @MockBean private Object object1;",
            "  @Autowired @MockBean private Object object2;",
            "  @MockBean private ImmutableList<Object> list;",
            "  @MockBean private String used;",
            "",
            "  void method() {",
            "     used = \"assignment\";",
            "     boolean a = used instanceof String;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "@MockBean({ImmutableList.class, Object.class})",
            "class A {",
            "",
            "  @MockBean private String used;",
            "",
            "  void method() {",
            "     used = \"assignment\";",
            "     boolean a = used instanceof String;",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  public void testReplacementMerge() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "@MockBean(ImmutableList.class)",
            "class A {",
            "  @MockBean private Object object;",
            "  @MockBean private ImmutableList<Object> list;",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "@MockBean({ImmutableList.class, Object.class})",
            "class A {",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  public void testReplacementMergeArray() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "@MockBean(classes = {ImmutableList.class, String.class})",
            "class A {",
            "  @MockBean private Object object;",
            "  @MockBean private ImmutableList<Object> list;",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.springframework.boot.test.mock.mockito.MockBean;",
            "",
            "@MockBean(",
            "   value = {ImmutableList.class, Object.class, String.class}",
            // Unconditionally setting classes = {} causes a problem in the other test...
            "   classes = {ImmutableList.class, String.class}",
            ")",
            "class A {",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
