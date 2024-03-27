package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

// XXX: There are no tests for multiple replacements within the same expression:
// - Error Prone doesn't currently support this, it seems.
// - The `BugCheckerRefactoringTestHelper` throws an exception in this case.
// - During actual compilation only the first replacement is applied.
// XXX: Can we perhaps work-around this by describing the fixes in reverse order?
final class PrimitiveComparisonTest {
  /**
   * Tests the identification of {@code byte} comparisons for which boxing can be avoided.
   *
   * @implNote The logic for {@code char} and {@code short} is exactly analogous to the {@code byte}
   *     case.
   */
  @Test
  void byteComparison() {
    CompilationTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.Comparator;
            import java.util.function.Function;

            class A {
              {
                // BUG: Diagnostic contains:
                Comparator.comparing(this::toPrimitive);
                Comparator.comparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                Comparator.comparing(o -> (byte) 0);
                Comparator.comparing(o -> (byte) 0, cmp());
                Comparator.comparing(this::toBoxed);
                Comparator.comparing(this::toBoxed, cmp());
                Comparator.comparing(o -> Byte.valueOf((byte) 0));
                Comparator.comparing(o -> Byte.valueOf((byte) 0), cmp());
                Comparator.comparing(toBoxed());
                Comparator.comparing(toBoxed(), cmp());
                Comparator.comparingInt(this::toPrimitive);
                Comparator.comparingInt(o -> (byte) 0);
                // BUG: Diagnostic contains:
                Comparator.comparingInt(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingInt(o -> Byte.valueOf((byte) 0));
                // BUG: Diagnostic contains:
                Comparator.comparingLong(this::toPrimitive);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(o -> (byte) 0);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(o -> Byte.valueOf((byte) 0));
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toPrimitive);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> (byte) 0);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> Byte.valueOf((byte) 0));

                // BUG: Diagnostic contains:
                cmp().thenComparing(this::toPrimitive);
                cmp().thenComparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                cmp().thenComparing(o -> (byte) 0);
                cmp().thenComparing(o -> (byte) 0, cmp());
                cmp().thenComparing(this::toBoxed);
                cmp().thenComparing(this::toBoxed, cmp());
                cmp().thenComparing(o -> Byte.valueOf((byte) 0));
                cmp().thenComparing(o -> Byte.valueOf((byte) 0), cmp());
                cmp().thenComparing(toBoxed());
                cmp().thenComparing(toBoxed(), cmp());
                cmp().thenComparingInt(this::toPrimitive);
                cmp().thenComparingInt(o -> (byte) 0);
                // BUG: Diagnostic contains:
                cmp().thenComparingInt(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingInt(o -> Byte.valueOf((byte) 0));
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(this::toPrimitive);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(o -> (byte) 0);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(o -> Byte.valueOf((byte) 0));
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toPrimitive);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> (byte) 0);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> Byte.valueOf((byte) 0));
              }

              private Comparator<Object> cmp() {
                return null;
              }

              private byte toPrimitive(Object o) {
                return 0;
              }

              private Byte toBoxed(Object o) {
                return 0;
              }

              private Function<Object, Byte> toBoxed() {
                return o -> 0;
              }
            }
            """)
        .doTest();
  }

  @Test
  void intComparison() {
    CompilationTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.Comparator;
            import java.util.function.Function;
            import java.util.function.ToIntFunction;

            class A {
              {
                // BUG: Diagnostic contains:
                Comparator.comparing(this::toPrimitive);
                Comparator.comparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                Comparator.comparing(o -> 0);
                Comparator.comparing(o -> 0, cmp());
                Comparator.comparing(this::toBoxed);
                Comparator.comparing(this::toBoxed, cmp());
                Comparator.comparing(o -> Integer.valueOf(0));
                Comparator.comparing(o -> Integer.valueOf(0), cmp());
                Comparator.comparing(toBoxed());
                Comparator.comparing(toBoxed(), cmp());
                Comparator.comparingInt(this::toPrimitive);
                Comparator.comparingInt(o -> 0);
                Comparator.comparingInt(toPrimitive());
                // BUG: Diagnostic contains:
                Comparator.comparingInt(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingInt(o -> Integer.valueOf(0));
                // BUG: Diagnostic contains:
                Comparator.comparingLong(this::toPrimitive);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(o -> 0);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(o -> Integer.valueOf(0));
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toPrimitive);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> 0);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> Integer.valueOf(0));

                // BUG: Diagnostic contains:
                cmp().thenComparing(this::toPrimitive);
                cmp().thenComparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                cmp().thenComparing(o -> 0);
                cmp().thenComparing(o -> 0, cmp());
                cmp().thenComparing(this::toBoxed);
                cmp().thenComparing(this::toBoxed, cmp());
                cmp().thenComparing(o -> Integer.valueOf(0));
                cmp().thenComparing(o -> Integer.valueOf(0), cmp());
                cmp().thenComparing(toBoxed());
                cmp().thenComparing(toBoxed(), cmp());
                cmp().thenComparingInt(this::toPrimitive);
                cmp().thenComparingInt(o -> 0);
                cmp().thenComparingInt(toPrimitive());
                // BUG: Diagnostic contains:
                cmp().thenComparingInt(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingInt(o -> Integer.valueOf(0));
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(this::toPrimitive);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(o -> 0);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(o -> Integer.valueOf(0));
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toPrimitive);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> 0);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> Integer.valueOf(0));
              }

              private Comparator<Object> cmp() {
                return null;
              }

              private int toPrimitive(Object o) {
                return 0;
              }

              private Integer toBoxed(Object o) {
                return 0;
              }

              private Function<Object, Integer> toBoxed() {
                return o -> 0;
              }

              private ToIntFunction<Object> toPrimitive() {
                return o -> 0;
              }
            }
            """)
        .doTest();
  }

  @Test
  void longComparison() {
    CompilationTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.Comparator;
            import java.util.function.Function;
            import java.util.function.ToLongFunction;

            class A {
              {
                // BUG: Diagnostic contains:
                Comparator.comparing(this::toPrimitive);
                Comparator.comparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                Comparator.comparing(o -> 0L);
                Comparator.comparing(o -> 0L, cmp());
                Comparator.comparing(this::toBoxed);
                Comparator.comparing(this::toBoxed, cmp());
                Comparator.comparing(o -> Long.valueOf(0));
                Comparator.comparing(o -> Long.valueOf(0), cmp());
                Comparator.comparing(toBoxed());
                Comparator.comparing(toBoxed(), cmp());
                Comparator.comparingLong(this::toPrimitive);
                Comparator.comparingLong(o -> 0L);
                Comparator.comparingLong(toPrimitive());
                // BUG: Diagnostic contains:
                Comparator.comparingLong(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingLong(o -> Long.valueOf(0));
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toPrimitive);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> 0L);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> Long.valueOf(0));

                // BUG: Diagnostic contains:
                cmp().thenComparing(this::toPrimitive);
                cmp().thenComparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                cmp().thenComparing(o -> 0L);
                cmp().thenComparing(o -> 0L, cmp());
                cmp().thenComparing(this::toBoxed);
                cmp().thenComparing(this::toBoxed, cmp());
                cmp().thenComparing(o -> Long.valueOf(0));
                cmp().thenComparing(o -> Long.valueOf(0), cmp());
                cmp().thenComparing(toBoxed());
                cmp().thenComparing(toBoxed(), cmp());
                cmp().thenComparingLong(this::toPrimitive);
                cmp().thenComparingLong(o -> 0L);
                cmp().thenComparingLong(toPrimitive());
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingLong(o -> Long.valueOf(0));
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toPrimitive);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> 0L);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> Long.valueOf(0));
              }

              private Comparator<Object> cmp() {
                return null;
              }

              private long toPrimitive(Object o) {
                return 0L;
              }

              private Long toBoxed(Object o) {
                return 0L;
              }

              private Function<Object, Long> toBoxed() {
                return o -> 0L;
              }

              private ToLongFunction<Object> toPrimitive() {
                return o -> 0L;
              }
            }
            """)
        .doTest();
  }

  @Test
  void floatComparison() {
    CompilationTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.Comparator;
            import java.util.function.Function;

            class A {
              {
                // BUG: Diagnostic contains:
                Comparator.comparing(this::toPrimitive);
                Comparator.comparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                Comparator.comparing(o -> 0.0f);
                Comparator.comparing(o -> 0.0f, cmp());
                Comparator.comparing(this::toBoxed);
                Comparator.comparing(this::toBoxed, cmp());
                Comparator.comparing(o -> Float.valueOf(0));
                Comparator.comparing(o -> Float.valueOf(0), cmp());
                Comparator.comparing(toBoxed());
                Comparator.comparing(toBoxed(), cmp());
                Comparator.comparingDouble(this::toPrimitive);
                Comparator.comparingDouble(o -> 0.0f);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> Float.valueOf(0));

                // BUG: Diagnostic contains:
                cmp().thenComparing(this::toPrimitive);
                cmp().thenComparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                cmp().thenComparing(o -> 0.0f);
                cmp().thenComparing(o -> 0.0f, cmp());
                cmp().thenComparing(this::toBoxed);
                cmp().thenComparing(this::toBoxed, cmp());
                cmp().thenComparing(o -> Float.valueOf(0));
                cmp().thenComparing(o -> Float.valueOf(0), cmp());
                cmp().thenComparing(toBoxed());
                cmp().thenComparing(toBoxed(), cmp());
                cmp().thenComparingDouble(this::toPrimitive);
                cmp().thenComparingDouble(o -> 0.0f);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> Float.valueOf(0));
              }

              private Comparator<Object> cmp() {
                return null;
              }

              private float toPrimitive(Object o) {
                return 0.0f;
              }

              private Float toBoxed(Object o) {
                return 0.0f;
              }

              private Function<Object, Float> toBoxed() {
                return o -> 0.0f;
              }
            }
            """)
        .doTest();
  }

  @Test
  void doubleComparison() {
    CompilationTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.Comparator;
            import java.util.function.Function;
            import java.util.function.ToDoubleFunction;

            class A {
              {
                // BUG: Diagnostic contains:
                Comparator.comparing(this::toPrimitive);
                Comparator.comparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                Comparator.comparing(o -> 0.0);
                Comparator.comparing(o -> 0.0, cmp());
                Comparator.comparing(this::toBoxed);
                Comparator.comparing(this::toBoxed, cmp());
                Comparator.comparing(o -> Double.valueOf(0));
                Comparator.comparing(o -> Double.valueOf(0), cmp());
                Comparator.comparing(toBoxed());
                Comparator.comparing(toBoxed(), cmp());
                Comparator.comparingDouble(this::toPrimitive);
                Comparator.comparingDouble(o -> 0.0);
                Comparator.comparingDouble(toPrimitive());
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                Comparator.comparingDouble(o -> Double.valueOf(0));

                // BUG: Diagnostic contains:
                cmp().thenComparing(this::toPrimitive);
                cmp().thenComparing(this::toPrimitive, cmp());
                // BUG: Diagnostic contains:
                cmp().thenComparing(o -> 0.0);
                cmp().thenComparing(o -> 0.0, cmp());
                cmp().thenComparing(this::toBoxed);
                cmp().thenComparing(this::toBoxed, cmp());
                cmp().thenComparing(o -> Double.valueOf(0));
                cmp().thenComparing(o -> Double.valueOf(0), cmp());
                cmp().thenComparing(toBoxed());
                cmp().thenComparing(toBoxed(), cmp());
                cmp().thenComparingDouble(this::toPrimitive);
                cmp().thenComparingDouble(o -> 0.0);
                cmp().thenComparingDouble(toPrimitive());
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(this::toBoxed);
                // BUG: Diagnostic contains:
                cmp().thenComparingDouble(o -> Double.valueOf(0));
              }

              private Comparator<Object> cmp() {
                return null;
              }

              private double toPrimitive(Object o) {
                return 0.0;
              }

              private Double toBoxed(Object o) {
                return 0.0;
              }

              private Function<Object, Double> toBoxed() {
                return o -> 0.0;
              }

              private ToDoubleFunction<Object> toPrimitive() {
                return o -> 0.0;
              }
            }
            """)
        .doTest();
  }

  @Test
  void stringComparison() {
    CompilationTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.Comparator;
            import java.util.function.Function;

            class A {
              {
                Comparator.comparing(String::valueOf);
                Comparator.comparing(String::valueOf, cmp());
                Comparator.comparing(o -> String.valueOf(o));
                Comparator.comparing(o -> String.valueOf(o), cmp());
                Comparator.comparing(toStr());
                Comparator.comparing(toStr(), cmp());

                cmp().thenComparing(String::valueOf);
                cmp().thenComparing(String::valueOf, cmp());
                cmp().thenComparing(o -> String.valueOf(o));
                cmp().thenComparing(o -> String.valueOf(o), cmp());
                cmp().thenComparing(toStr());
                cmp().thenComparing(toStr(), cmp());
              }

              private Comparator<Object> cmp() {
                return null;
              }

              private Function<Object, String> toStr() {
                return String::valueOf;
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacementWithPrimitiveVariants() {
    BugCheckerRefactoringTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = Comparator.comparing(o -> (byte) 0);
              Comparator<A> bCmp2 = Comparator.<A, Byte>comparing(o -> (byte) 0);
              Comparator<A> cCmp = Comparator.comparing(o -> (char) 0);
              Comparator<A> cCmp2 = Comparator.<A, Character>comparing(o -> (char) 0);
              Comparator<A> sCmp = Comparator.comparing(o -> (short) 0);
              Comparator<A> sCmp2 = Comparator.<A, Short>comparing(o -> (short) 0);
              Comparator<A> iCmp = Comparator.comparing(o -> 0);
              Comparator<A> iCmp2 = Comparator.<A, Integer>comparing(o -> 0);
              Comparator<A> lCmp = Comparator.comparing(o -> 0L);
              Comparator<A> lCmp2 = Comparator.<A, Long>comparing(o -> 0L);
              Comparator<A> fCmp = Comparator.comparing(o -> 0.0f);
              Comparator<A> fCmp2 = Comparator.<A, Float>comparing(o -> 0.0f);
              Comparator<A> dCmp = Comparator.comparing(o -> 0.0);
              Comparator<A> dCmp2 = Comparator.<A, Double>comparing(o -> 0.0);

              default void m() {
                bCmp.<Byte>thenComparing(o -> (byte) 0);
                bCmp.thenComparing(o -> (byte) 0);
                cCmp.<Character>thenComparing(o -> (char) 0);
                cCmp.thenComparing(o -> (char) 0);
                sCmp.<Short>thenComparing(o -> (short) 0);
                sCmp.thenComparing(o -> (short) 0);
                iCmp.<Integer>thenComparing(o -> 0);
                iCmp.thenComparing(o -> 0);
                lCmp.<Long>thenComparing(o -> 0L);
                lCmp.thenComparing(o -> 0L);
                fCmp.<Float>thenComparing(o -> 0.0f);
                fCmp.thenComparing(o -> 0.0f);
                dCmp.<Double>thenComparing(o -> 0.0);
                dCmp.thenComparing(o -> 0.0);
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = Comparator.comparingInt(o -> (byte) 0);
              Comparator<A> bCmp2 = Comparator.<A>comparingInt(o -> (byte) 0);
              Comparator<A> cCmp = Comparator.comparingInt(o -> (char) 0);
              Comparator<A> cCmp2 = Comparator.<A>comparingInt(o -> (char) 0);
              Comparator<A> sCmp = Comparator.comparingInt(o -> (short) 0);
              Comparator<A> sCmp2 = Comparator.<A>comparingInt(o -> (short) 0);
              Comparator<A> iCmp = Comparator.comparingInt(o -> 0);
              Comparator<A> iCmp2 = Comparator.<A>comparingInt(o -> 0);
              Comparator<A> lCmp = Comparator.comparingLong(o -> 0L);
              Comparator<A> lCmp2 = Comparator.<A>comparingLong(o -> 0L);
              Comparator<A> fCmp = Comparator.comparingDouble(o -> 0.0f);
              Comparator<A> fCmp2 = Comparator.<A>comparingDouble(o -> 0.0f);
              Comparator<A> dCmp = Comparator.comparingDouble(o -> 0.0);
              Comparator<A> dCmp2 = Comparator.<A>comparingDouble(o -> 0.0);

              default void m() {
                bCmp.thenComparingInt(o -> (byte) 0);
                bCmp.thenComparingInt(o -> (byte) 0);
                cCmp.thenComparingInt(o -> (char) 0);
                cCmp.thenComparingInt(o -> (char) 0);
                sCmp.thenComparingInt(o -> (short) 0);
                sCmp.thenComparingInt(o -> (short) 0);
                iCmp.thenComparingInt(o -> 0);
                iCmp.thenComparingInt(o -> 0);
                lCmp.thenComparingLong(o -> 0L);
                lCmp.thenComparingLong(o -> 0L);
                fCmp.thenComparingDouble(o -> 0.0f);
                fCmp.thenComparingDouble(o -> 0.0f);
                dCmp.thenComparingDouble(o -> 0.0);
                dCmp.thenComparingDouble(o -> 0.0);
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithBoxedVariants() {
    BugCheckerRefactoringTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = Comparator.comparingInt(o -> Byte.valueOf((byte) 0));
              Comparator<A> bCmp2 = Comparator.<A>comparingInt(o -> Byte.valueOf((byte) 0));
              Comparator<A> cCmp = Comparator.comparingInt(o -> Character.valueOf((char) 0));
              Comparator<A> cCmp2 = Comparator.<A>comparingInt(o -> Character.valueOf((char) 0));
              Comparator<A> sCmp = Comparator.comparingInt(o -> Short.valueOf((short) 0));
              Comparator<A> sCmp2 = Comparator.<A>comparingInt(o -> Short.valueOf((short) 0));
              Comparator<A> iCmp = Comparator.comparingInt(o -> Integer.valueOf(0));
              Comparator<A> iCmp2 = Comparator.<A>comparingInt(o -> Integer.valueOf(0));
              Comparator<A> lCmp = Comparator.comparingLong(o -> Long.valueOf(0));
              Comparator<A> lCmp2 = Comparator.<A>comparingLong(o -> Long.valueOf(0));
              Comparator<A> fCmp = Comparator.comparingDouble(o -> Float.valueOf(0));
              Comparator<A> fCmp2 = Comparator.<A>comparingDouble(o -> Float.valueOf(0));
              Comparator<A> dCmp = Comparator.comparingDouble(o -> Double.valueOf(0));
              Comparator<A> dCmp2 = Comparator.<A>comparingDouble(o -> Double.valueOf(0));

              default void m() {
                bCmp.thenComparingInt(o -> Byte.valueOf((byte) 0));
                cCmp.thenComparingInt(o -> Character.valueOf((char) 0));
                sCmp.thenComparingInt(o -> Short.valueOf((short) 0));
                iCmp.thenComparingInt(o -> Integer.valueOf(0));
                lCmp.thenComparingLong(o -> Long.valueOf(0));
                fCmp.thenComparingDouble(o -> Float.valueOf(0));
                dCmp.thenComparingDouble(o -> Double.valueOf(0));
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = Comparator.comparing(o -> Byte.valueOf((byte) 0));
              Comparator<A> bCmp2 = Comparator.<A, Byte>comparing(o -> Byte.valueOf((byte) 0));
              Comparator<A> cCmp = Comparator.comparing(o -> Character.valueOf((char) 0));
              Comparator<A> cCmp2 = Comparator.<A, Character>comparing(o -> Character.valueOf((char) 0));
              Comparator<A> sCmp = Comparator.comparing(o -> Short.valueOf((short) 0));
              Comparator<A> sCmp2 = Comparator.<A, Short>comparing(o -> Short.valueOf((short) 0));
              Comparator<A> iCmp = Comparator.comparing(o -> Integer.valueOf(0));
              Comparator<A> iCmp2 = Comparator.<A, Integer>comparing(o -> Integer.valueOf(0));
              Comparator<A> lCmp = Comparator.comparing(o -> Long.valueOf(0));
              Comparator<A> lCmp2 = Comparator.<A, Long>comparing(o -> Long.valueOf(0));
              Comparator<A> fCmp = Comparator.comparing(o -> Float.valueOf(0));
              Comparator<A> fCmp2 = Comparator.<A, Float>comparing(o -> Float.valueOf(0));
              Comparator<A> dCmp = Comparator.comparing(o -> Double.valueOf(0));
              Comparator<A> dCmp2 = Comparator.<A, Double>comparing(o -> Double.valueOf(0));

              default void m() {
                bCmp.thenComparing(o -> Byte.valueOf((byte) 0));
                cCmp.thenComparing(o -> Character.valueOf((char) 0));
                sCmp.thenComparing(o -> Short.valueOf((short) 0));
                iCmp.thenComparing(o -> Integer.valueOf(0));
                lCmp.thenComparing(o -> Long.valueOf(0));
                fCmp.thenComparing(o -> Float.valueOf(0));
                dCmp.thenComparing(o -> Double.valueOf(0));
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithPrimitiveVariantsUsingStaticImports() {
    BugCheckerRefactoringTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static java.util.Comparator.comparing;

            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = comparing(o -> (byte) 0);
              Comparator<A> cCmp = comparing(o -> (char) 0);
              Comparator<A> sCmp = comparing(o -> (short) 0);
              Comparator<A> iCmp = comparing(o -> 0);
              Comparator<A> lCmp = comparing(o -> 0L);
              Comparator<A> fCmp = comparing(o -> 0.0f);
              Comparator<A> dCmp = comparing(o -> 0.0);
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static java.util.Comparator.comparing;
            import static java.util.Comparator.comparingDouble;
            import static java.util.Comparator.comparingInt;
            import static java.util.Comparator.comparingLong;

            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = comparingInt(o -> (byte) 0);
              Comparator<A> cCmp = comparingInt(o -> (char) 0);
              Comparator<A> sCmp = comparingInt(o -> (short) 0);
              Comparator<A> iCmp = comparingInt(o -> 0);
              Comparator<A> lCmp = comparingLong(o -> 0L);
              Comparator<A> fCmp = comparingDouble(o -> 0.0f);
              Comparator<A> dCmp = comparingDouble(o -> 0.0);
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithBoxedVariantsUsingStaticImports() {
    BugCheckerRefactoringTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static java.util.Comparator.comparingDouble;
            import static java.util.Comparator.comparingInt;
            import static java.util.Comparator.comparingLong;

            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = comparingInt(o -> Byte.valueOf((byte) 0));
              Comparator<A> cCmp = comparingInt(o -> Character.valueOf((char) 0));
              Comparator<A> sCmp = comparingInt(o -> Short.valueOf((short) 0));
              Comparator<A> iCmp = comparingInt(o -> Integer.valueOf(0));
              Comparator<A> lCmp = comparingLong(o -> Long.valueOf(0));
              Comparator<A> fCmp = comparingDouble(o -> Float.valueOf(0));
              Comparator<A> dCmp = comparingDouble(o -> Double.valueOf(0));
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static java.util.Comparator.comparing;
            import static java.util.Comparator.comparingDouble;
            import static java.util.Comparator.comparingInt;
            import static java.util.Comparator.comparingLong;

            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = comparing(o -> Byte.valueOf((byte) 0));
              Comparator<A> cCmp = comparing(o -> Character.valueOf((char) 0));
              Comparator<A> sCmp = comparing(o -> Short.valueOf((short) 0));
              Comparator<A> iCmp = comparing(o -> Integer.valueOf(0));
              Comparator<A> lCmp = comparing(o -> Long.valueOf(0));
              Comparator<A> fCmp = comparing(o -> Float.valueOf(0));
              Comparator<A> dCmp = comparing(o -> Double.valueOf(0));
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithPrimitiveVariantsInComplexSyntacticalContext() {
    BugCheckerRefactoringTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> (byte) 0);
              Comparator<A> cCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> (char) 0);
              Comparator<A> sCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> (short) 0);
              Comparator<A> iCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> 0);
              Comparator<A> lCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> 0L);
              Comparator<A> fCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> 0.0f);
              Comparator<A> dCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> 0.0);
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp = Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> (byte) 0);
              Comparator<A> cCmp = Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> (char) 0);
              Comparator<A> sCmp = Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> (short) 0);
              Comparator<A> iCmp = Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> 0);
              Comparator<A> lCmp = Comparator.<A, A>comparing(o -> o).thenComparingLong(o -> 0L);
              Comparator<A> fCmp = Comparator.<A, A>comparing(o -> o).thenComparingDouble(o -> 0.0f);
              Comparator<A> dCmp = Comparator.<A, A>comparing(o -> o).thenComparingDouble(o -> 0.0);
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithBoxedVariantsInComplexSyntacticalContext() {
    BugCheckerRefactoringTestHelper.newInstance(PrimitiveComparison.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> Byte.valueOf((byte) 0));
              Comparator<A> cCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> Character.valueOf((char) 0));
              Comparator<A> sCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> Short.valueOf((short) 0));
              Comparator<A> iCmp = Comparator.<A, A>comparing(o -> o).thenComparingInt(o -> Integer.valueOf(0));
              Comparator<A> lCmp = Comparator.<A, A>comparing(o -> o).thenComparingLong(o -> Long.valueOf(0));
              Comparator<A> fCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparingDouble(o -> Float.valueOf(0));
              Comparator<A> dCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparingDouble(o -> Double.valueOf(0));
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.util.Comparator;

            interface A extends Comparable<A> {
              Comparator<A> bCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparing(o -> Byte.valueOf((byte) 0));
              Comparator<A> cCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparing(o -> Character.valueOf((char) 0));
              Comparator<A> sCmp =
                  Comparator.<A, A>comparing(o -> o).thenComparing(o -> Short.valueOf((short) 0));
              Comparator<A> iCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> Integer.valueOf(0));
              Comparator<A> lCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> Long.valueOf(0));
              Comparator<A> fCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> Float.valueOf(0));
              Comparator<A> dCmp = Comparator.<A, A>comparing(o -> o).thenComparing(o -> Double.valueOf(0));
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
