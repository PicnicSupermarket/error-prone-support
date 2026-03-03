package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class LexicographicalPermitsListingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(LexicographicalPermitsListing.class, getClass())
        .addSourceLines(
            "A.java",
            "interface A {",
            "  non-sealed class X extends UnsortedPermitsClass",
            "      implements SinglePermits, SortedPermits, UnsortedPermits {}",
            "",
            "  non-sealed class Y extends UnsortedPermitsClass implements SortedPermits, UnsortedPermits {}",
            "",
            "  non-sealed class Z extends UnsortedPermitsClass {}",
            "",
            "  sealed interface NoPermits {",
            "    record R() implements NoPermits {}",
            "  }",
            "",
            "  sealed interface SinglePermits permits X {}",
            "",
            "  sealed interface SortedPermits permits X, Y {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  sealed interface UnsortedPermits permits Y, X {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  sealed class UnsortedPermitsClass permits Z, X, Y {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(LexicographicalPermitsListing.class, getClass())
        .addInputLines(
            "A.java",
            "interface A {",
            "  non-sealed class X extends UnsortedPermitsClass implements UnsortedPermits {}",
            "",
            "  non-sealed class Y extends UnsortedPermitsClass implements UnsortedPermits {}",
            "",
            "  non-sealed class Z extends UnsortedPermitsClass {}",
            "",
            "  sealed interface UnsortedPermits permits Y, X {}",
            "",
            "  sealed class UnsortedPermitsClass permits Z, X, Y {}",
            "}")
        .addOutputLines(
            "A.java",
            "interface A {",
            "  non-sealed class X extends UnsortedPermitsClass implements UnsortedPermits {}",
            "",
            "  non-sealed class Y extends UnsortedPermitsClass implements UnsortedPermits {}",
            "",
            "  non-sealed class Z extends UnsortedPermitsClass {}",
            "",
            "  sealed interface UnsortedPermits permits X, Y {}",
            "",
            "  sealed class UnsortedPermitsClass permits X, Y, Z {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
