package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MockitoMockClassReferenceTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MockitoMockClassReference.class, getClass())
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
            "    // BUG: Diagnostic contains:",
            "    List<String> genericallyTypedMock = mock(List.class);",
            "    var nonExplicitlyTypedMock = mock(Integer.class);",
            "    Class<? extends Number> variableType = Integer.class;",
            "    Number variableTypedMock = mock(variableType);",
            "    Integer namedMock = mock(Integer.class, \"name\");",
            "    Object subtypeMock = mock(Integer.class);",
            "",
            "    // BUG: Diagnostic contains:",
            "    String equalTypedSpy = spy(String.class);",
            "    // BUG: Diagnostic contains:",
            "    List nonGenericallyTypedSpy = spy(List.class);",
            "    // BUG: Diagnostic contains:",
            "    List<String> genericallyTypedSpy = spy(List.class);",
            "    var nonExplicitlyTypedSpy = spy(Integer.class);",
            "    Number subtypeSpy = spy(Integer.class);",
            "    Object objectSpy = spy(new Object());",
            "  }",
            "",
            "  Integer getIntegerMock() {",
            "    // BUG: Diagnostic contains:",
            "    return mock(Integer.class);",
            "  }",
            "",
            "  <T> T getGenericMock(Class<T> clazz) {",
            "    return mock(clazz);",
            "  }",
            "",
            "  Number getSubTypeMock() {",
            "    return mock(Integer.class);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(MockitoMockClassReference.class, getClass())
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
            "",
            "    var unknownTypeMock = mock(Integer.class);",
            "    Integer namedMock = mock(Integer.class, \"name\");",
            "",
            "    Runnable runnableSpy = spy(Runnable.class);",
            "    List<String> listOfStringsSpy = spy(List.class);",
            "    List genericListSpy = spy(List.class);",
            "",
            "    var unknownTypeSpy = spy(Integer.class);",
            "    Object objectSpy = spy(new Object());",
            "  }",
            "",
            "  Integer getIntegerMock() {",
            "    return mock(Integer.class);",
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
            "",
            "    var unknownTypeMock = mock(Integer.class);",
            "    Integer namedMock = mock(Integer.class, \"name\");",
            "",
            "    Runnable runnableSpy = spy();",
            "    List<String> listOfStringsSpy = spy();",
            "    List genericListSpy = spy();",
            "",
            "    var unknownTypeSpy = spy(Integer.class);",
            "    Object objectSpy = spy(new Object());",
            "  }",
            "",
            "  Integer getIntegerMock() {",
            "    return mock();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
