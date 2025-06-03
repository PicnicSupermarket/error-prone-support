package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ImmutablesPartialEqualityTest {
  @Test
  void flagsComparisonOfImplementationOfInterfaceWithAuxiliaryField() {
    CompilationTestHelper.newInstance(ImmutablesPartialEquality.class, getClass())
        .addSourceLines(
            "JavaClassATest.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import javax.annotation.concurrent.Immutable;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            "interface JavaInterfaceA {",
            "  String getId();",
            "",
            "  @Value.Auxiliary",
            "  String getAux();",
            "}",
            "",
            "@Immutable",
            "final class JavaClassA implements JavaInterfaceA {",
            "  @Override public String getId() { return \"\"; }",
            "  @Override public String getAux() { return \"\"; }",
            "}",
            "",
            "class JavaClassATest {",
            "  void testEqualityUsingIsEqualTo() {",
            "    // BUG: Diagnostic contains: Immutables with partial equality should not be compared using isEqualTo in tests",
            "    assertThat(new JavaClassA()).isEqualTo(new JavaClassA());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void flagsEqualityComparisonOnAbstractAndConcreteImmutableWithEqualsOverride() {
    CompilationTestHelper.newInstance(ImmutablesPartialEquality.class, getClass())
        .addSourceLines(
            "JavaClassBTest.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import javax.annotation.concurrent.Immutable;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            "abstract class AbstractJavaClassB {",
            "  abstract String id();",
            "",
            "  @Override public boolean equals(Object o) { return false; }",
            "  @Override public int hashCode() { return 42; }",
            "}",
            "",
            "@Immutable",
            "final class JavaClassB extends AbstractJavaClassB {",
            "  @Override public String id() { return \"impl\"; }",
            "}",
            "",
            "class JavaClassBTest {",
            "  void testEqualityUsingIsEqualTo() {",
            "    // BUG: Diagnostic contains: Immutables with partial equality should not be compared using isEqualTo in tests",
            "    assertThat(new JavaClassB()).isEqualTo(new JavaClassB());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void flagsEqualityComparisonOnInterfaceImmutablePartTwo() {
    CompilationTestHelper.newInstance(ImmutablesPartialEquality.class, getClass())
        .addSourceLines(
            "JavaClassCTest.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import javax.annotation.concurrent.Immutable;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            "interface SellingUnitInterface {",
            "  int getId();",
            "",
            "  @Value.Auxiliary",
            "  String getName();",
            "}",
            "",
            "@Immutable",
            "final class SellingUnit implements SellingUnitInterface {",
            "  @Override public int getId() { return 42; }",
            "  @Override public String getName() { return \"\"; }",
            "}",
            "",
            "class JavaClassCTest {",
            "  void testEqualityUsingIsEqualTo() {",
            "    SellingUnitInterface firstSU = new SellingUnit();",
            "    SellingUnitInterface secondSU = new SellingUnit();",
                "    // BUG: Diagnostic contains: Immutables with partial equality should not be compared using isEqualTo in tests",
            "    assertThat(firstSU).isEqualTo(secondSU);",
            "  }",
            "}")
        .doTest();
  }
}
