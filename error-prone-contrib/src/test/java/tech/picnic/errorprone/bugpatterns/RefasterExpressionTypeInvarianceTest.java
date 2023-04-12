package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterExpressionTypeInvarianceTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(RefasterExpressionTypeInvariance.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  @BeforeTemplate",
            "  Object before(String str) {",
            "    if (str == null) {",
            "      return null;",
            "    }",
            "    if (str != null) {",
            "      return (CharSequence) null;",
            "    }",
            "    return str;",
            "  }",
            "}")
        .doTest();
  }
}
