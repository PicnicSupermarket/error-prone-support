package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import org.testng.annotations.Test;

public final class PicnicConfigClassCheckTest {
  @Test
  public void testNonFinalWithAnnotation() {
    BugCheckerRefactoringTestHelper.newInstance(new PicnicConfigClassCheck(), getClass())
        .addInputLines(
            "TestConfig.java",
            "import org.springframework.context.annotation.Configuration;",
            "@Configuration",
            "public class TestConfig {}")
        .addOutputLines(
            "TestConfig.java",
            "import org.springframework.context.annotation.Configuration;",
            "public final class TestConfig {}")
        .doTest();
  }

  @Test
  public void tesetFinalWithoutAnnotation() {
    BugCheckerRefactoringTestHelper.newInstance(new PicnicConfigClassCheck(), getClass())
        .addInputLines("TestConfig.java", "", "public final class TestConfig {}")
        .addOutputLines("TestConfig.java", "", "public final class TestConfig {}")
        .doTest();
  }

  @Test
  public void testNonFinalWithoutAnnotation() {
    BugCheckerRefactoringTestHelper.newInstance(new PicnicConfigClassCheck(), getClass())
        .addInputLines("TestConfig.java", "public class TestConfig {}")
        .addOutputLines("TestConfig.java", "public final class TestConfig {}")
        .doTest();
  }

  @Test
  public void testInnerClass() {
    BugCheckerRefactoringTestHelper.newInstance(new PicnicConfigClassCheck(), getClass())
        .addInputLines(
            "TestClass.java",
            "import org.springframework.context.annotation.Configuration;",
            "public class TestClass {",
            "  @Configuration",
            "  static class InnerTestConfig {}",
            "}")
        .addOutputLines(
            "TestClass.java",
            "import org.springframework.context.annotation.Configuration;",
            "public class TestClass {",
            "  static final class InnerTestConfig {}",
            "}")
        .doTest();
  }
}
