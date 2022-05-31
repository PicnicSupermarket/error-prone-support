package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.containsPattern;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class MissingImmutableSortedSetDefaultCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MissingImmutableSortedSetDefaultCheck.class, getClass())
          .expectErrorMessage(
              "X",
              containsPattern(
                  "Methods returning an `ImmutableSortedSet` within an @Value.Immutable or @Value.Modifiable class should provide a default value or specify the comparator."));
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(
          MissingImmutableSortedSetDefaultCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Immutable",
            "interface A {",
            "    // BUG: Diagnostic matches: X",
            "    ImmutableSortedSet<String> sortedSet();",
            "    default ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    default ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> defaultSortedSet3();",
            "}",
            "",
            "@Value.Modifiable",
            "interface B {",
            "    // BUG: Diagnostic matches: X",
            "    ImmutableSortedSet<String> sortedSet();",
            "    default ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    default ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> defaultSortedSet3();",
            "}",
            "",
            "@Value.Immutable",
            "abstract class C {",
            "    // BUG: Diagnostic matches: X",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}",
            "",
            "@Value.Modifiable",
            "abstract class D {",
            "    // BUG: Diagnostic matches: X",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}",
            "",
            "abstract class E {",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .doTest();
  }

  @Test
  void replacementInImmutableInterface() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Immutable",
            "interface A {",
            "    ImmutableSortedSet<String> sortedSet();",
            "    default ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    default ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .addOutputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Immutable",
            "interface A {",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> sortedSet();",
            "    default ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    default ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementInModifiableInterface() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Modifiable",
            "interface B {",
            "    ImmutableSortedSet<String> sortedSet();",
            "    default ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    default ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .addOutputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Modifiable",
            "interface B {",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> sortedSet();",
            "    default ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    default ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementInImmutableAbstractClass() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Immutable",
            "abstract class C {",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .addOutputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Immutable",
            "abstract class C {",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementInModifiableAbstractClass() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Modifiable",
            "abstract class D {",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .addOutputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "@Value.Modifiable",
            "abstract class D {",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void noReplacementInNormalClass() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "abstract class E {",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .addOutputLines(
            "A.java",
            "import org.immutables.value.Value;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "abstract class E {",
            "    abstract ImmutableSortedSet<String> sortedSet();",
            "    ImmutableSortedSet<String> defaultSortedSet() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.Default",
            "    ImmutableSortedSet<String> defaultSortedSet2() {",
            "       return ImmutableSortedSet.of();",
            "    }",
            "    @Value.NaturalOrder",
            "    abstract ImmutableSortedSet<String> defaultSortedSet3();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
