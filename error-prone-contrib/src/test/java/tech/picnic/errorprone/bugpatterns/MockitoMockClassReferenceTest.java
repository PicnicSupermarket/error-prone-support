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
            """
            import static org.mockito.Mockito.mock;
            import static org.mockito.Mockito.spy;
            import static org.mockito.Mockito.withSettings;

            import java.util.List;
            import java.util.Objects;
            import org.mockito.invocation.InvocationOnMock;

            class A {
              {
                Double d = Objects.requireNonNullElseGet(null, () -> mock(Double.class));
                Double d2 =
                    Objects.requireNonNullElseGet(
                        null,
                        () -> {
                          return mock(Double.class);
                        });
              }

              void m() {
                Number variableMock = 42;
                // BUG: Diagnostic contains:
                variableMock = mock(Number.class);
                // BUG: Diagnostic contains:
                variableMock = mock(Number.class, "name");
                // BUG: Diagnostic contains:
                variableMock = mock(Number.class, InvocationOnMock::callRealMethod);
                // BUG: Diagnostic contains:
                variableMock = mock(Number.class, withSettings());
                variableMock = mock(Integer.class);
                variableMock = 42;
                // BUG: Diagnostic contains:
                List rawMock = mock(List.class);
                // BUG: Diagnostic contains:
                List<String> genericMock = mock(List.class);
                var varMock = mock(Integer.class);
                Class<? extends Number> numberType = Integer.class;
                Number variableTypeMock = mock(numberType);
                Object subtypeMock = mock(Integer.class);

                Number variableSpy = 42;
                // BUG: Diagnostic contains:
                variableSpy = spy(Number.class);
                variableSpy = spy(Integer.class);
                variableSpy = 42;
                // BUG: Diagnostic contains:
                List rawSpy = spy(List.class);
                // BUG: Diagnostic contains:
                List<String> genericSpy = spy(List.class);
                var varSpy = spy(Integer.class);
                Number variableTypeSpy = spy(numberType);
                Object subtypeSpy = spy(Integer.class);
                Object objectSpy = spy(new Object());

                Objects.hash(mock(Integer.class));
                Integer i = mock(mock(Integer.class));
                String s = new String(mock(String.class));
              }

              Double getDoubleMock() {
                return Objects.requireNonNullElseGet(
                    null,
                    () -> {
                      return mock(Double.class);
                    });
              }

              Integer getIntegerMock() {
                // BUG: Diagnostic contains:
                return mock(Integer.class);
              }

              <T> T getGenericMock(Class<T> clazz) {
                return mock(clazz);
              }

              Number getSubTypeMock() {
                return mock(Integer.class);
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(MockitoMockClassReference.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static org.mockito.Mockito.mock;
            import static org.mockito.Mockito.spy;
            import static org.mockito.Mockito.withSettings;

            import org.mockito.invocation.InvocationOnMock;

            class A {
              void m() {
                Number simpleMock = mock(Number.class);
                Number namedMock = mock(Number.class, "name");
                Number customAnswerMock = mock(Number.class, InvocationOnMock::callRealMethod);
                Number customSettingsMock = mock(Number.class, withSettings());
                Number simpleSpy = spy(Number.class);
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static org.mockito.Mockito.mock;
            import static org.mockito.Mockito.spy;
            import static org.mockito.Mockito.withSettings;

            import org.mockito.invocation.InvocationOnMock;

            class A {
              void m() {
                Number simpleMock = mock();
                Number namedMock = mock("name");
                Number customAnswerMock = mock(InvocationOnMock::callRealMethod);
                Number customSettingsMock = mock(withSettings());
                Number simpleSpy = spy();
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
