package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class LexicographicalSealedInterfacePermitsListingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(
            LexicographicalSealedInterfacePermitsListing.class, getClass())
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import pkg.A.B;",
            "import pkg.A.C;",
            "import pkg.A.D;",
            "",
            "// BUG: Diagnostic contains:",
            "public sealed interface A permits C, D, B {",
            "",
            "  non-sealed interface B extends A, E, F {}",
            "",
            "  non-sealed interface C extends A, E, F {}",
            "",
            "  non-sealed interface D extends A, E, F {}",
            "",
            "  sealed interface E permits B, C, D {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  sealed interface F permits C, B, D {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(
            LexicographicalSealedInterfacePermitsListing.class, getClass())
        .addInputLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import pkg.A.B;",
            "import pkg.A.C;",
            "import pkg.A.D;",
            "",
            "public sealed interface A permits C, D, B {",
            "",
            "  non-sealed interface B extends A, E, F {}",
            "",
            "  non-sealed interface C extends A, E, F {}",
            "",
            "  non-sealed interface D extends A, E, F {}",
            "",
            "  sealed interface E permits B, C, D {}",
            "",
            "  sealed interface F permits C, B, D {}",
            "}")
        .addOutputLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import pkg.A.B;",
            "import pkg.A.C;",
            "import pkg.A.D;",
            "",
            "public sealed interface A permits B, C, D {",
            "",
            "  non-sealed interface B extends A, E, F {}",
            "",
            "  non-sealed interface C extends A, E, F {}",
            "",
            "  non-sealed interface D extends A, E, F {}",
            "",
            "  sealed interface E permits B, C, D {}",
            "",
            "  sealed interface F permits B, C, D {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
