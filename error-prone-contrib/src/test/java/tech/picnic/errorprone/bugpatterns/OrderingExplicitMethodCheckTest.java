package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class OrderingExplicitMethodCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(OrderingExplicitMethodCheck.class, getClass());

  @Test
  void testHandleExplicitForEnums() {
    compilationTestHelper
        .addSourceLines(
            "in/A.java",
            "import com.google.common.collect.Ordering;",
            "import java.util.Comparator;",
            "",
            "enum A {",
            "    ONE, TWO, THREE",
            "}",
            "",
            "class B {",
            "    // BUG: Diagnostic contains: Method should include all values from A enum",
            "    Comparator<A> explicit = Ordering.explicit(A.TWO, A.ONE);",
            "}")
        .doTest();
  }
}
