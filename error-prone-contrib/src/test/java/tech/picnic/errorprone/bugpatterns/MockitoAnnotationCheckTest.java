package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class MockitoAnnotationCheckTest {
    private final CompilationTestHelper compilationTestHelper =
            CompilationTestHelper.newInstance(MockitoAnnotationCheck.class, getClass());
    private final BugCheckerRefactoringTestHelper refactoringTestHelper =
            BugCheckerRefactoringTestHelper.newInstance(new MockitoAnnotationCheck(), getClass());

    @Test
    public void testIdentification() {
        compilationTestHelper
                .addSourceLines(
                        "A.java",
                        "import static org.mockito.Mockito.mock;",
                        "",
                        "import org.junit.jupiter.api.Tag;",
                        "import org.junit.jupiter.api.Test;",
                        "",
                        "@Tag(\"unit\")",
                        "class MockitoTest {",
                        "",
                        "    @Test",
                        "    void mockitoTest() {",
                        "        mock(String.class);",
                        "    }",
                        "}")
                .doTest();
    }

    @Test
    public void testReplacement() {
        refactoringTestHelper
                .addInputLines(
                        "in/A.java",
                        "import static org.mockito.Mockito.mock;",
                        "",
                        "import org.junit.jupiter.api.Tag;",
                        "import org.junit.jupiter.api.Test;",
                        "",
                        "@Tag(\"unit\")",
                        "class MockitoTest {",
                        "",
                        "    @Test",
                        "    void mockitoTest() {",
                        "        mock(String.class);",
                        "    }",
                        "}")
                .addOutputLines(
                        "out/A.java",
                        "import static org.mockito.Mockito.mock;",
                        "import static org.mockito.quality.Strictness.STRICT_STUBS;",
                        "",
                        "import org.junit.jupiter.api.Tag;",
                        "import org.junit.jupiter.api.Test;",
                        "import org.mockito.junit.jupiter.MockitoSettings;",
                        "",
                        "@Tag(\"unit\")",
                        "@MockitoSettings(strictness = STRICT_STUBS)",
                        "class MockitoTest {",
                        "",
                        "    @Test",
                        "    void mockitoTest() {",
                        "        mock(String.class);",
                        "    }",
                        "}")
                .doTest(TestMode.TEXT_MATCH);
    }
}
