package tech.picnic.errorprone.experimental.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MethodReferenceUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MethodReferenceUsage.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.common.collect.Streams;
            import java.util.HashMap;
            import java.util.Map;
            import java.util.function.IntConsumer;
            import java.util.function.IntFunction;
            import java.util.stream.Stream;

            class A {
              private final Stream<Integer> s = Stream.of(1);
              private final Map<Integer, Integer> m = new HashMap<>();
              private final Runnable thrower =
                  () -> {
                    throw new RuntimeException();
                  };

              void unaryExternalStaticFunctionCalls() {
                s.forEach(String::valueOf);
                // BUG: Diagnostic contains:
                s.forEach(v -> String.valueOf(v));
                s.forEach(
                    // BUG: Diagnostic contains:
                    (v) -> {
                      String.valueOf(v);
                    });
                s.forEach(
                    // BUG: Diagnostic contains:
                    (Integer v) -> {
                      {
                        String.valueOf(v);
                      }
                    });
                s.forEach(
                    v -> {
                      String.valueOf(v);
                      String.valueOf(v);
                    });

                s.map(String::valueOf);
                // BUG: Diagnostic contains:
                s.map(v -> String.valueOf(v));
                // BUG: Diagnostic contains:
                s.map((v) -> (String.valueOf(v)));
                s.map(
                    // BUG: Diagnostic contains:
                    (Integer v) -> {
                      return String.valueOf(v);
                    });
                s.map(
                    // BUG: Diagnostic contains:
                    (final Integer v) -> {
                      return (String.valueOf(v));
                    });
                s.map(
                    v -> {
                      String.valueOf(v);
                      return String.valueOf(v);
                    });

                s.findFirst().orElseGet(() -> Integer.valueOf("0"));
                m.forEach((k, v) -> String.valueOf(v));
                m.forEach((k, v) -> String.valueOf(k));
              }

              void binaryExternalInstanceFunctionCalls() {
                m.forEach(m::put);
                // BUG: Diagnostic contains:
                m.forEach((k, v) -> m.put(k, v));
                m.forEach((k, v) -> m.put(v, k));
                m.forEach(
                    // BUG: Diagnostic contains:
                    (Integer k, Integer v) -> {
                      m.put(k, v);
                    });
                m.forEach(
                    (k, v) -> {
                      m.put(k, k);
                    });
                m.forEach(
                    // BUG: Diagnostic contains:
                    (final Integer k, final Integer v) -> {
                      {
                        m.put(k, v);
                      }
                    });
                m.forEach(
                    (k, v) -> {
                      {
                        m.put(v, v);
                      }
                    });
                m.forEach((k, v) -> new HashMap<Integer, Integer>().put(k, v));
                m.forEach(
                    (k, v) -> {
                      m.put(k, v);
                      m.put(k, v);
                    });

                Streams.zip(s, s, m::put);
                // BUG: Diagnostic contains:
                Streams.zip(s, s, (a, b) -> m.put(a, b));
                Streams.zip(s, s, (a, b) -> m.put(b, a));
                // BUG: Diagnostic contains:
                Streams.zip(s, s, (Integer a, Integer b) -> (m.put(a, b)));
                Streams.zip(s, s, (a, b) -> (m.put(a, a)));
                Streams.zip(
                    s,
                    s,
                    // BUG: Diagnostic contains:
                    (final Integer a, final Integer b) -> {
                      return m.put(a, b);
                    });
                Streams.zip(
                    s,
                    s,
                    (a, b) -> {
                      return m.put(b, b);
                    });
                Streams.zip(
                    s,
                    s,
                    // BUG: Diagnostic contains:
                    (a, b) -> {
                      return (m.put(a, b));
                    });
                Streams.zip(
                    s,
                    s,
                    (a, b) -> {
                      return (m.put(b, a));
                    });
                Streams.zip(
                    s,
                    s,
                    (a, b) -> {
                      m.put(a, b);
                      return m.put(a, b);
                    });
              }

              void nullaryExternalInstanceFunctionCalls() {
                s.map(Integer::doubleValue);
                // BUG: Diagnostic contains:
                s.map(i -> i.doubleValue());
                s.map(i -> i.toString());
                s.map(i -> s.toString());

                // BUG: Diagnostic contains:
                Stream.of(int.class).filter(c -> c.isEnum());
                Stream.of((Class<?>) int.class).filter(Class::isEnum);
                // BUG: Diagnostic contains:
                Stream.of((Class<?>) int.class).filter(c -> c.isEnum());
              }

              void localFunctionCalls() {
                s.forEach(v -> ivoid0());
                s.forEach(v -> iint0());
                s.forEach(v -> svoid0());
                s.forEach(v -> sint0());

                s.forEach(this::ivoid1);
                // BUG: Diagnostic contains:
                s.forEach(v -> ivoid1(v));
                s.forEach(
                    // BUG: Diagnostic contains:
                    v -> {
                      ivoid1(v);
                    });
                s.forEach(this::iint1);
                // BUG: Diagnostic contains:
                s.forEach(v -> iint1(v));
                s.forEach(
                    // BUG: Diagnostic contains:
                    v -> {
                      iint1(v);
                    });

                s.forEach(A::svoid1);
                // BUG: Diagnostic contains:
                s.forEach(v -> svoid1(v));
                s.forEach(
                    // BUG: Diagnostic contains:
                    v -> {
                      svoid1(v);
                    });
                s.forEach(A::sint1);
                // BUG: Diagnostic contains:
                s.forEach(v -> sint1(v));
                s.forEach(
                    // BUG: Diagnostic contains:
                    v -> {
                      sint1(v);
                    });

                s.forEach(v -> ivoid2(v, v));
                s.forEach(v -> iint2(v, v));
                s.forEach(v -> svoid2(v, v));
                s.forEach(v -> sint2(v, v));

                m.forEach((k, v) -> ivoid0());
                m.forEach((k, v) -> iint0());
                m.forEach((k, v) -> svoid0());
                m.forEach((k, v) -> sint0());

                m.forEach(this::ivoid2);
                // BUG: Diagnostic contains:
                m.forEach((k, v) -> ivoid2(k, v));
                m.forEach(
                    // BUG: Diagnostic contains:
                    (k, v) -> {
                      ivoid2(k, v);
                    });
                m.forEach(this::iint2);
                // BUG: Diagnostic contains:
                m.forEach((k, v) -> iint2(k, v));
                m.forEach(
                    // BUG: Diagnostic contains:
                    (k, v) -> {
                      iint2(k, v);
                    });

                m.forEach(A::svoid2);
                // BUG: Diagnostic contains:
                m.forEach((k, v) -> svoid2(k, v));
                m.forEach(
                    // BUG: Diagnostic contains:
                    (k, v) -> {
                      svoid2(k, v);
                    });
                m.forEach(A::sint2);
                // BUG: Diagnostic contains:
                m.forEach((k, v) -> sint2(k, v));
                m.forEach(
                    // BUG: Diagnostic contains:
                    (k, v) -> {
                      sint2(k, v);
                    });
              }

              void functionCallsWhoseReplacementWouldBeAmbiguous() {
                receiver(
                    i -> {
                      Integer.toString(i);
                    });
              }

              void assortedOtherEdgeCases() {
                s.forEach(v -> String.valueOf(v.toString()));
                TernaryOp o1 = (a, b, c) -> String.valueOf(a);
                TernaryOp o2 = (a, b, c) -> String.valueOf(b);
                TernaryOp o3 = (a, b, c) -> String.valueOf(c);
                TernaryOp o4 = (a, b, c) -> c.concat(a);
                TernaryOp o5 = (a, b, c) -> c.concat(b);
                TernaryOp o6 = (a, b, c) -> a.concat(c);
                TernaryOp o7 = (a, b, c) -> b.concat(c);
              }

              void receiver(IntFunction<?> op) {}

              void receiver(IntConsumer op) {}

              void ivoid0() {}

              void ivoid1(int a) {}

              void ivoid2(int a, int b) {}

              int iint0() {
                return 0;
              }

              int iint1(int a) {
                return 0;
              }

              int iint2(int a, int b) {
                return 0;
              }

              static void svoid0() {}

              static void svoid1(int a) {}

              static void svoid2(int a, int b) {}

              static void svoid3(int a, int b, int c) {}

              static int sint0() {
                return 0;
              }

              static int sint1(int a) {
                return 0;
              }

              static int sint2(int a, int b) {
                return 0;
              }

              interface TernaryOp {
                String collect(String a, String b, String c);
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(MethodReferenceUsage.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static java.util.Collections.emptyList;

            import java.util.Collections;
            import java.util.List;
            import java.util.Map;
            import java.util.function.IntSupplier;
            import java.util.function.Supplier;
            import java.util.stream.Stream;

            class A {
              static class B extends A {
                final A a = new B();
                final B b = new B();

                IntSupplier intSup;
                Supplier<List<?>> listSup;

                void m() {
                  intSup = () -> a.iint0();
                  intSup = () -> b.iint0();
                  intSup = () -> this.iint0();
                  intSup = () -> super.iint0();

                  intSup = () -> a.sint0();
                  intSup = () -> b.sint0();
                  intSup = () -> this.sint0();
                  intSup = () -> super.sint0();
                  intSup = () -> A.sint0();
                  intSup = () -> B.sint0();

                  listSup = () -> Collections.emptyList();
                  listSup = () -> emptyList();

                  Stream.of((Class<?>) int.class).filter(c -> c.isEnum());
                  Stream.of((Map<?, ?>) null).map(Map::keySet).map(s -> s.size());
                }

                @Override
                int iint0() {
                  return 0;
                }
              }

              int iint0() {
                return 0;
              }

              static int sint0() {
                return 0;
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static java.util.Collections.emptyList;

            import java.util.Collections;
            import java.util.List;
            import java.util.Map;
            import java.util.Set;
            import java.util.function.IntSupplier;
            import java.util.function.Supplier;
            import java.util.stream.Stream;

            class A {
              static class B extends A {
                final A a = new B();
                final B b = new B();

                IntSupplier intSup;
                Supplier<List<?>> listSup;

                void m() {
                  intSup = a::iint0;
                  intSup = b::iint0;
                  intSup = this::iint0;
                  intSup = super::iint0;

                  intSup = () -> a.sint0();
                  intSup = () -> b.sint0();
                  intSup = () -> this.sint0();
                  intSup = () -> super.sint0();
                  intSup = A::sint0;
                  intSup = B::sint0;

                  listSup = Collections::emptyList;
                  listSup = Collections::emptyList;

                  Stream.of((Class<?>) int.class).filter(Class::isEnum);
                  Stream.of((Map<?, ?>) null).map(Map::keySet).map(Set::size);
                }

                @Override
                int iint0() {
                  return 0;
                }
              }

              int iint0() {
                return 0;
              }

              static int sint0() {
                return 0;
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
