package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MockitoMockClassReferenceTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MockitoMockClassReference.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(MockitoMockClassReference.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.spy;",
            "",
            "import java.util.List;",
            "",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  String memberMock = mock(String.class);",
            "  // BUG: Diagnostic contains:",
            "  String memberSpy = mock(String.class);",
            "",
            "  void m() {",
            "    Integer variableMock;",
            "    // BUG: Diagnostic contains:",
            "    variableMock = mock(Integer.class);",
            "    // BUG: Diagnostic contains:",
            "    List nonGenericallyTypedMock = mock(List.class);",
            "    var dynamicallyTypedMock = mock(Integer.class);",
            "    Integer namedMock = mock(Integer.class, \"name\");",
            "    Object subtypeMock = mock(Integer.class);",
            "",
            "    // BUG: Diagnostic contains:",
            "    String equalTypedSpy = spy(String.class);",
            "    // BUG: Diagnostic contains:",
            "    List nonGenericallyTypedSpy = spy(List.class);",
            "    var dynamicallyTypedSpy = spy(Integer.class);",
            "    Object objectSpy = spy(new Object());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.spy;",
            "",
            "import java.util.List;",
            "",
            "class A {",
            "  String memberMock = mock(String.class);",
            "",
            "  void m() {",
            "    Runnable runnableMock;",
            "    runnableMock = mock(Runnable.class);",
            "    List<String> listOfStringsMock = mock(List.class);",
            "    List genericListMock = mock(List.class);",
            "    var unknownTypeMock = mock(Integer.class);",
            "    Integer namedMock = mock(Integer.class, \"name\");",
            "",
            "    Runnable runnableSpy = spy(Runnable.class);",
            "    List<String> listOfStringsSpy = spy(List.class);",
            "    List genericListSpy = spy(List.class);",
            "    var unknownTypeSpy = spy(Integer.class);",
            "    Object objectSpy = spy(new Object());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.spy;",
            "",
            "import java.util.List;",
            "",
            "class A {",
            "  String memberMock = mock();",
            "",
            "  void m() {",
            "    Runnable runnableMock;",
            "    runnableMock = mock();",
            "    List<String> listOfStringsMock = mock();",
            "    List genericListMock = mock();",
            "    var unknownTypeMock = mock(Integer.class);",
            "    Integer namedMock = mock(Integer.class, \"name\");",
            "",
            "    Runnable runnableSpy = spy();",
            "    List<String> listOfStringsSpy = spy();",
            "    List genericListSpy = spy();",
            "    var unknownTypeSpy = spy(Integer.class);",
            "    Object objectSpy = spy(new Object());",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
