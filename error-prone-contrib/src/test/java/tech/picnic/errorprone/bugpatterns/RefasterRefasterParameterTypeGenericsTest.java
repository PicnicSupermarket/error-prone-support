package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterRefasterParameterTypeGenericsTest {
  // XXX: Rename inner class.
  @Test
  void identification() {
    CompilationTestHelper.newInstance(RefasterParameterTypeGenerics.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import java.util.function.Function;",
            "",
            "class A {",
            "  static class B<I, O> {",
            "    @BeforeTemplate",
            "    // BUG: Diagnostic contains:",
            "    void before(Function<? super I, ? extends O> function) {}",
            "  }",
            "}")
        .doTest();
  }
}
