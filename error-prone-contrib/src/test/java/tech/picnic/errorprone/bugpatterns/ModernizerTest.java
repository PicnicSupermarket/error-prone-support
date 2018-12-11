package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ModernizerTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(Modernizer.class, getClass());

  // XXX: Also add calls that should not be flagged.
  // XXX: Test extension, field references, instance methods, static methods.
  // methods with primitives, primitive arrays, references, reference arrays
  // zero, one two args.
  // XXX: Also test constructors!
  // XXX: Test that the appropriate "prefer" message is emitted.
  // XXX: List the test cases in `ModernizerTest`?

  @Test
  void fieldIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.base.Charsets.ISO_8859_1;",
            "",
            "import com.google.common.base.Charsets;",
            "import java.nio.charset.StandardCharsets;",
            "",
            "class A {",
            "  {",
            "    // BUG: Diagnostic contains: Prefer java.nio.charset.StandardCharsets",
            "    Object o1 = ISO_8859_1;",
            "    // BUG: Diagnostic contains: Prefer java.nio.charset.StandardCharsets",
            "    Object o2 = Charsets.ISO_8859_1;",
            "    // BUG: Diagnostic contains: Prefer java.nio.charset.StandardCharsets",
            "    Object o3 = com.google.common.base.Charsets.ISO_8859_1;",
            "",
            "    Object o4 = StandardCharsets.ISO_8859_1;",
            "    Object o5 = java.nio.charset.StandardCharsets.ISO_8859_1;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void nullaryMethodIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.base.Optional.absent;",
            "",
            "import com.google.common.base.Optional;",
            "import java.util.function.Supplier;",
            "",
            "class A {",
            "  {",
            "    // BUG: Diagnostic contains: Prefer java.util.Optional",
            "    absent();",
            "    // BUG: Diagnostic contains: Prefer java.util.Optional",
            "    Optional.absent();",
            "    // BUG: Diagnostic contains: Prefer java.util.Optional",
            "    com.google.common.base.Optional.absent();",
            "    // BUG: Diagnostic contains: Prefer java.util.Optional",
            "    Supplier<?> s1 = Optional::absent;",
            "    // BUG: Diagnostic contains: Prefer java.util.Optional",
            "    Supplier<?> s2 = com.google.common.base.Optional::absent;",
            "",
            "    java.util.Optional.empty();",
            "    Supplier<?> s3 = java.util.Optional::empty;",
            "",
            "    Dummy.absent();",
            "  }",
            "",
            "  static final class Dummy {",
            "    static Optional<?> absent() {",
            "      return null;",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void unaryMethodWithIntegerArgumentIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.Lists.newArrayListWithCapacity;",
            "",
            "import com.google.common.collect.Lists;",
            "import java.util.ArrayList;",
            "import java.util.function.IntFunction;",
            "",
            "class A {",
            "  {",
            "    // BUG: Diagnostic contains: Prefer java.util.ArrayList<>(int)",
            "    newArrayListWithCapacity(0);",
            "    // BUG: Diagnostic contains: Prefer java.util.ArrayList<>(int)",
            "    Lists.newArrayListWithCapacity(1);",
            "    // BUG: Diagnostic contains: Prefer java.util.ArrayList<>(int)",
            "    com.google.common.collect.Lists.newArrayListWithCapacity(2);",
            "    // BUG: Diagnostic contains: Prefer java.util.ArrayList<>(int)",
            "    IntFunction<?> f1 = Lists::newArrayListWithCapacity;",
            "    // BUG: Diagnostic contains: Prefer java.util.ArrayList<>(int)",
            "    IntFunction<?> f2 = com.google.common.collect.Lists::newArrayListWithCapacity;",
            "",
            "    new ArrayList<>(3);",
            "    IntFunction<?> f3 = ArrayList::new;",
            "    IntFunction<?> f4 = java.util.ArrayList::new;",
            "",
            "    Dummy.newArrayListWithCapacity(4);",
            "  }",
            "",
            "  static final class Dummy {",
            "    static ArrayList<?> newArrayListWithCapacity(int initialArraySize) {",
            "      return null;",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void binaryMethodWithObjectArgumentsIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.base.Objects.equal;",
            "",
            "import com.google.common.base.Objects;",
            "import java.util.function.BiPredicate;",
            "",
            "class A {",
            "  {",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.equals(Object, Object)",
            "    equal(null, null);",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.equals(Object, Object)",
            "    Objects.equal(null, null);",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.equals(Object, Object)",
            "    com.google.common.base.Objects.equal(null, null);",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.equals(Object, Object)",
            "    BiPredicate<?, ?> p1 = Objects::equal;",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.equals(Object, Object)",
            "    BiPredicate<?, ?> p2 = com.google.common.base.Objects::equal;",
            "",
            "    java.util.Objects.equals(null, null);",
            "    BiPredicate<?, ?> p3 = java.util.Objects::equals;",
            "",
            "    Dummy.equal(null, null);",
            "  }",
            "",
            "  static final class Dummy {",
            "    static boolean equal(Object a, Object b) {",
            "      return false;",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void varargsMethodWithObjectArgumentsIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.base.Objects;",
            "import java.util.function.ToIntFunction;",
            "",
            "class A {",
            "  {",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.hash(Object...)",
            "    Objects.hashCode((Object) null);",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.hash(Object...)",
            "    com.google.common.base.Objects.hashCode(null, null);",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.hash(Object...)",
            "    ToIntFunction<?> f1 = Objects::hashCode;",
            "    // BUG: Diagnostic contains: Prefer java.util.Objects.hash(Object...)",
            "    ToIntFunction<?> f2 = com.google.common.base.Objects::hashCode;",
            "",
            "    java.util.Objects.hash(null, null, null);",
            "    ToIntFunction<?> f3 = java.util.Objects::hash;",
            "",
            "    Dummy.hashCode(null, null, null, null);",
            "  }",
            "",
            "  static final class Dummy {",
            "    static int hashCode(Object... objects) {",
            "      return 0;",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void binaryConstructorWithByteArrayAndObjectArgumentsIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.io.UnsupportedEncodingException;",
            "import java.nio.charset.StandardCharsets;",
            "",
            "class A {",
            "  void m() throws UnsupportedEncodingException {",
            "    // BUG: Diagnostic contains: Prefer java.lang.String.<init>(byte[], java.nio.charset.Charset)",
            "    new String(new byte[0], \"\");",
            "    // BUG: Diagnostic contains: Prefer java.lang.String.<init>(byte[], java.nio.charset.Charset)",
            "    new java.lang.String(new byte[] {}, toString());",
            "",
            "    new String(new byte[0], StandardCharsets.UTF_8);",
            "    new java.lang.String(new byte[0], StandardCharsets.UTF_8);",
            "",
            "    new Dummy(new byte[0], \"\");",
            "  }",
            "",
            "  static final class Dummy {",
            "    Dummy(byte bytes[], String charsetName) {}",
            "  }",
            "}")
        .doTest();
  }
}
