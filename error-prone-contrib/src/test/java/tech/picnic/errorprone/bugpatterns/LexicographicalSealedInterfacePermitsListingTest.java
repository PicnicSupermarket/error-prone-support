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
            "A.java",
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
            "",
            "  class G {}",
            "}")
        .addSourceLines(
            "Foo.java",
            "package pkg;",
            "",
            "import pkg.Foo.Bar;",
            "import pkg.Foo.Baz;",
            "import pkg.Foo.Qux;",
            "",
            "// BUG: Diagnostic contains:",
            "public sealed class Foo permits Qux, Bar, Baz {",
            "",
            "  static final class Bar extends Foo {}",
            "",
            "  static final class Baz extends Foo {}",
            "",
            "  static final class Qux extends Foo {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(
            LexicographicalSealedInterfacePermitsListing.class, getClass())
        .addInputLines(
            "A.java",
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
            "A.java",
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
        .addInputLines(
            "B.java",
            "package pkg;",
            "",
            "import pkg.B.Bar;",
            "import pkg.B.Baz;",
            "import pkg.B.Foo;",
            "",
            "public sealed class B permits Baz, Foo, Bar {",
            "",
            "  static final class Foo extends B {}",
            "",
            "  static final class Bar extends B {}",
            "",
            "  static final class Baz extends B {}",
            "}")
        .addOutputLines(
            "B.java",
            "package pkg;",
            "",
            "import pkg.B.Bar;",
            "import pkg.B.Baz;",
            "import pkg.B.Foo;",
            "",
            "public sealed class B permits Bar, Baz, Foo {",
            "",
            "  static final class Foo extends B {}",
            "",
            "  static final class Bar extends B {}",
            "",
            "  static final class Baz extends B {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
