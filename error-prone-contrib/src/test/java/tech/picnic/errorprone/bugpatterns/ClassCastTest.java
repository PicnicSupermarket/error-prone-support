package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ClassCastTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ClassCast.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.Optional;",
            "import java.util.Set;",
            "",
            "class A {",
            "  void m() {",
            "    Number foo = 0;",
            "    Number bar = 1;",
            "",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(foo).map(i -> (Integer) i);",
            "",
            "    Optional.of(foo).map(i -> 2);",
            "    Optional.of(foo).map(i -> (Integer) 2);",
            "    Optional.of(foo).map(i -> bar);",
            "    Optional.of(foo).map(i -> (Integer) bar);",
            "",
            "    ImmutableList.of(Set.of(foo)).stream().map(l -> (ImmutableSet<Number>) l);",
            "    ImmutableList.of(Set.of(foo)).stream().map(l -> (ImmutableSet<?>) l);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ClassCast.class, getClass())
        .addInputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).map(i -> (Integer) i);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).map(Integer.class::cast);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
