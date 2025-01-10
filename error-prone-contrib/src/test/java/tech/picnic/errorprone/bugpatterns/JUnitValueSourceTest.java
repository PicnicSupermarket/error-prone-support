package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitValueSourceTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(JUnitValueSource.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static org.junit.jupiter.params.provider.Arguments.arguments;

            import java.util.Optional;
            import java.util.stream.Stream;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.Arguments;
            import org.junit.jupiter.params.provider.MethodSource;

            class A {
              private static Stream<Arguments> identificationTestCases() {
                return Stream.of(arguments(1), Arguments.of(2));
              }

              @ParameterizedTest
              // BUG: Diagnostic contains:
              @MethodSource("identificationTestCases")
              void identification(int foo) {}

              private static int[] identificationWithParensTestCases() {
                return new int[] {1, 2};
              }

              @ParameterizedTest
              // BUG: Diagnostic contains:
              @MethodSource("identificationWithParensTestCases()")
              void identificationWithParens(int foo) {}

              @ParameterizedTest
              @MethodSource("valueFactoryMissingTestCases")
              void valueFactoryMissing(int foo) {}

              private static Stream<Arguments> multipleUsagesTestCases() {
                return Stream.of(arguments(1), Arguments.of(2));
              }

              @ParameterizedTest
              @MethodSource("multipleUsagesTestCases")
              void multipleUsages1(int foo) {}

              @ParameterizedTest
              @MethodSource("multipleUsagesTestCases()")
              void multipleUsages2(int bar) {}

              private static Stream<Arguments> valueFactoryRepeatedTestCases() {
                return Stream.of(arguments(1), arguments(2));
              }

              @ParameterizedTest
              @MethodSource({"valueFactoryRepeatedTestCases", "valueFactoryRepeatedTestCases"})
              void valueFactoryRepeated(int foo) {}

              private static Stream<Arguments> multipleParametersTestCases() {
                return Stream.of(arguments(1, 2), arguments(3, 4));
              }

              @ParameterizedTest
              @MethodSource("multipleParametersTestCases")
              void multipleParameters(int first, int second) {}

              private static int[] arrayWithoutInitializersTestCases() {
                return new int[1];
              }

              @ParameterizedTest
              @MethodSource("arrayWithoutInitializersTestCases")
              void arrayWithoutInitializers(int foo) {}

              private static Stream<Arguments> runtimeValueTestCases() {
                int second = 2;
                return Stream.of(arguments(1), arguments(second));
              }

              @ParameterizedTest
              @MethodSource("runtimeValueTestCases")
              void runtimeValue(int foo) {}

              private static Stream<Arguments> streamChainTestCases() {
                return Stream.of(1, 2).map(Arguments::arguments);
              }

              @ParameterizedTest
              @MethodSource("streamChainTestCases")
              void streamChain(int number) {}

              private static Stream<Arguments> multipleReturnsTestCases() {
                if (true) {
                  return Stream.of(arguments(1), arguments(2));
                } else {
                  return Stream.of(arguments(3), arguments(4));
                }
              }

              @ParameterizedTest
              @MethodSource("multipleReturnsTestCases")
              void multipleReturns(int number) {}

              private static Stream<Arguments> multipleFactoriesFooTestCases() {
                return Stream.of(arguments(1));
              }

              private static Stream<Arguments> multipleFactoriesBarTestCases() {
                return Stream.of(arguments(1));
              }

              @ParameterizedTest
              @MethodSource({"multipleFactoriesFooTestCases", "multipleFactoriesBarTestCases"})
              void multipleFactories(int i) {}

              private static Stream<Arguments> extraArgsTestCases() {
                return Stream.of(arguments(1), arguments(1, 2));
              }

              @ParameterizedTest
              @MethodSource("extraArgsTestCases")
              void extraArgs(int... i) {}

              private static Stream<Arguments> localClassTestCases() {
                class Foo {
                  Stream<Arguments> foo() {
                    return Stream.of(arguments(1), arguments(2));
                  }
                }
                return Stream.of(arguments(1), arguments(2));
              }

              @ParameterizedTest
              // BUG: Diagnostic contains:
              @MethodSource("localClassTestCases")
              void localClass(int i) {}

              private static Stream<Arguments> lambdaReturnTestCases() {
                int foo =
                    Optional.of(10)
                        .map(
                            i -> {
                              return i / 2;
                            })
                        .orElse(0);
                return Stream.of(arguments(1), arguments(1));
              }

              @ParameterizedTest
              // BUG: Diagnostic contains:
              @MethodSource("lambdaReturnTestCases")
              void lambdaReturn(int i) {}

              @ParameterizedTest
              @MethodSource("tech.picnic.errorprone.Foo#fooTestCases")
              void staticMethodReference(int foo) {}

              private static Stream<Arguments> valueFactoryWithArgumentTestCases(int amount) {
                return Stream.of(arguments(1), arguments(2));
              }

              @ParameterizedTest
              // BUG: Diagnostic contains:
              @MethodSource("valueFactoryWithArgumentTestCases")
              void valueFactoryWithArgument(int foo) {}

              private static Arguments[] emptyArrayValueFactoryTestCases() {
                return new Arguments[] {};
              }

              @ParameterizedTest
              @MethodSource("emptyArrayValueFactoryTestCases")
              void emptyArrayValueFactory(int foo) {}

              private static Stream<Arguments> emptyStreamValueFactoryTestCases() {
                return Stream.of();
              }

              @ParameterizedTest
              @MethodSource("emptyStreamValueFactoryTestCases")
              void emptyStreamValueFactory(int foo) {}

              private static Arguments[] invalidValueFactoryArgumentsTestCases() {
                return new Arguments[] {arguments(1), arguments(new Object() {})};
              }

              @ParameterizedTest
              @MethodSource("invalidValueFactoryArgumentsTestCases")
              void invalidValueFactoryArguments(int foo) {}
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(JUnitValueSource.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static org.junit.jupiter.params.provider.Arguments.arguments;

            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableSet;
            import java.util.List;
            import java.util.Set;
            import java.util.stream.DoubleStream;
            import java.util.stream.IntStream;
            import java.util.stream.LongStream;
            import java.util.stream.Stream;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.Arguments;
            import org.junit.jupiter.params.provider.MethodSource;

            class A {
              private static final boolean CONST_BOOLEAN = false;
              private static final byte CONST_BYTE = 42;
              private static final char CONST_CHARACTER = 'a';
              private static final short CONST_SHORT = 42;
              private static final int CONST_INTEGER = 42;
              private static final long CONST_LONG = 42;
              private static final float CONST_FLOAT = 42;
              private static final double CONST_DOUBLE = 42;
              private static final String CONST_STRING = "foo";

              private static Stream<Arguments> streamOfBooleanArguments() {
                return Stream.of(arguments(false), arguments(true), arguments(CONST_BOOLEAN));
              }

              @ParameterizedTest
              @MethodSource("streamOfBooleanArguments")
              void primitiveBoolean(boolean b) {}

              private static Stream<Object> streamOfBooleansAndBooleanArguments() {
                return Stream.of(false, arguments(true), CONST_BOOLEAN);
              }

              @ParameterizedTest
              @MethodSource("streamOfBooleansAndBooleanArguments")
              void boxedBoolean(Boolean b) {}

              private static List<Arguments> listOfByteArguments() {
                return List.of(arguments((byte) 0), arguments((byte) 1), arguments(CONST_BYTE));
              }

              @ParameterizedTest
              @MethodSource("listOfByteArguments")
              void primitiveByte(byte b) {}

              private static List<Object> listOfBytesAndByteArguments() {
                return List.of((byte) 0, arguments((byte) 1), CONST_BYTE);
              }

              @ParameterizedTest
              @MethodSource("listOfBytesAndByteArguments")
              void boxedByte(Byte b) {}

              private static Set<Arguments> setOfCharacterArguments() {
                return Set.of(arguments((char) 0), arguments((char) 1), arguments(CONST_CHARACTER));
              }

              @ParameterizedTest
              @MethodSource("setOfCharacterArguments")
              void primitiveCharacter(char c) {}

              private static Set<Object> setOfCharactersAndCharacterArguments() {
                return Set.of((char) 0, arguments((char) 1), CONST_CHARACTER);
              }

              @ParameterizedTest
              @MethodSource("setOfCharactersAndCharacterArguments")
              void boxedCharacter(Character c) {}

              private static Arguments[] arrayOfShortArguments() {
                return new Arguments[] {arguments((short) 0), arguments((short) 1), arguments(CONST_SHORT)};
              }

              @ParameterizedTest
              @MethodSource("arrayOfShortArguments")
              void primitiveShort(short s) {}

              private static Object[] arrayOfShortsAndShortArguments() {
                return new Object[] {(short) 0, arguments((short) 1), CONST_SHORT};
              }

              @ParameterizedTest
              @MethodSource("arrayOfShortsAndShortArguments")
              void boxedShort(Short s) {}

              private static IntStream intStream() {
                return IntStream.of(0, 1, CONST_INTEGER);
              }

              @ParameterizedTest
              @MethodSource("intStream")
              void primitiveInteger(int i) {}

              private static int[] intArray() {
                return new int[] {0, 1, CONST_INTEGER};
              }

              @ParameterizedTest
              @MethodSource("intArray")
              void boxedInteger(Integer i) {}

              private static LongStream longStream() {
                return LongStream.of(0, 1, CONST_LONG);
              }

              @ParameterizedTest
              @MethodSource("longStream")
              void primitiveLong(long l) {}

              private static long[] longArray() {
                return new long[] {0, 1, CONST_LONG};
              }

              @ParameterizedTest
              @MethodSource("longArray")
              void boxedLong(Long l) {}

              private static ImmutableList<Arguments> immutableListOfFloatArguments() {
                return ImmutableList.of(arguments(0.0F), arguments(1.0F), arguments(CONST_FLOAT));
              }

              @ParameterizedTest
              @MethodSource("immutableListOfFloatArguments")
              void primitiveFloat(float f) {}

              private static Stream<Object> streamOfFloatsAndFloatArguments() {
                return Stream.of(0.0F, arguments(1.0F), CONST_FLOAT);
              }

              @ParameterizedTest
              @MethodSource("streamOfFloatsAndFloatArguments")
              void boxedFloat(Float f) {}

              private static DoubleStream doubleStream() {
                return DoubleStream.of(0, 1, CONST_DOUBLE);
              }

              @ParameterizedTest
              @MethodSource("doubleStream")
              void primitiveDouble(double d) {}

              private static double[] doubleArray() {
                return new double[] {0, 1, CONST_DOUBLE};
              }

              @ParameterizedTest
              @MethodSource("doubleArray")
              void boxedDouble(Double d) {}

              private static ImmutableSet<Arguments> immutableSetOfStringArguments() {
                return ImmutableSet.of(arguments("foo"), arguments("bar"), arguments(CONST_STRING));
              }

              @ParameterizedTest
              @MethodSource("immutableSetOfStringArguments")
              void string(String s) {}

              private static Stream<Class<?>> streamOfClasses() {
                return Stream.of(Stream.class, java.util.Map.class);
              }

              @ParameterizedTest
              @MethodSource("streamOfClasses")
              void clazz(Class<?> c) {}

              private static Stream<Arguments> sameNameFactoryTestCases() {
                return Stream.of(arguments(1));
              }

              private static Stream<Arguments> sameNameFactoryTestCases(int overload) {
                return Stream.of(arguments(overload));
              }

              @ParameterizedTest
              @MethodSource("sameNameFactoryTestCases")
              void sameNameFactory(int i) {}
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static org.junit.jupiter.params.provider.Arguments.arguments;

            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableSet;
            import java.util.List;
            import java.util.Set;
            import java.util.stream.DoubleStream;
            import java.util.stream.IntStream;
            import java.util.stream.LongStream;
            import java.util.stream.Stream;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.Arguments;
            import org.junit.jupiter.params.provider.MethodSource;
            import org.junit.jupiter.params.provider.ValueSource;

            class A {
              private static final boolean CONST_BOOLEAN = false;
              private static final byte CONST_BYTE = 42;
              private static final char CONST_CHARACTER = 'a';
              private static final short CONST_SHORT = 42;
              private static final int CONST_INTEGER = 42;
              private static final long CONST_LONG = 42;
              private static final float CONST_FLOAT = 42;
              private static final double CONST_DOUBLE = 42;
              private static final String CONST_STRING = "foo";

              @ParameterizedTest
              @ValueSource(booleans = {false, true, CONST_BOOLEAN})
              void primitiveBoolean(boolean b) {}

              @ParameterizedTest
              @ValueSource(booleans = {false, true, CONST_BOOLEAN})
              void boxedBoolean(Boolean b) {}

              @ParameterizedTest
              @ValueSource(bytes = {(byte) 0, (byte) 1, CONST_BYTE})
              void primitiveByte(byte b) {}

              @ParameterizedTest
              @ValueSource(bytes = {(byte) 0, (byte) 1, CONST_BYTE})
              void boxedByte(Byte b) {}

              @ParameterizedTest
              @ValueSource(chars = {(char) 0, (char) 1, CONST_CHARACTER})
              void primitiveCharacter(char c) {}

              @ParameterizedTest
              @ValueSource(chars = {(char) 0, (char) 1, CONST_CHARACTER})
              void boxedCharacter(Character c) {}

              @ParameterizedTest
              @ValueSource(shorts = {(short) 0, (short) 1, CONST_SHORT})
              void primitiveShort(short s) {}

              @ParameterizedTest
              @ValueSource(shorts = {(short) 0, (short) 1, CONST_SHORT})
              void boxedShort(Short s) {}

              @ParameterizedTest
              @ValueSource(ints = {0, 1, CONST_INTEGER})
              void primitiveInteger(int i) {}

              @ParameterizedTest
              @ValueSource(ints = {0, 1, CONST_INTEGER})
              void boxedInteger(Integer i) {}

              @ParameterizedTest
              @ValueSource(longs = {0, 1, CONST_LONG})
              void primitiveLong(long l) {}

              @ParameterizedTest
              @ValueSource(longs = {0, 1, CONST_LONG})
              void boxedLong(Long l) {}

              @ParameterizedTest
              @ValueSource(floats = {0.0F, 1.0F, CONST_FLOAT})
              void primitiveFloat(float f) {}

              @ParameterizedTest
              @ValueSource(floats = {0.0F, 1.0F, CONST_FLOAT})
              void boxedFloat(Float f) {}

              @ParameterizedTest
              @ValueSource(doubles = {0, 1, CONST_DOUBLE})
              void primitiveDouble(double d) {}

              @ParameterizedTest
              @ValueSource(doubles = {0, 1, CONST_DOUBLE})
              void boxedDouble(Double d) {}

              @ParameterizedTest
              @ValueSource(strings = {"foo", "bar", CONST_STRING})
              void string(String s) {}

              @ParameterizedTest
              @ValueSource(classes = {Stream.class, java.util.Map.class})
              void clazz(Class<?> c) {}

              private static Stream<Arguments> sameNameFactoryTestCases(int overload) {
                return Stream.of(arguments(overload));
              }

              @ParameterizedTest
              @ValueSource(ints = 1)
              void sameNameFactory(int i) {}
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
