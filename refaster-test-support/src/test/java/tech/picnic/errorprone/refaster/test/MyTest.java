package tech.picnic.errorprone.refaster.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

public class MyTest extends MyAbstractTest {
  public static final class MyTestInstanceFactory implements TestInstanceFactory {
    @Override
    public Object createTestInstance(
        TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
        throws TestInstantiationException {
      return new MySubTest();
    }
  }

  // XXX: Name
  @Test
  void foo() {
    // Options:
    // 1. Add examples to the Refaster rule, generate the tests from those
    //
    // 2. Define as test, and use a custom test annotation, which:
    // - Generates the relevant test resources
    // - Executes them.
    // ^ Might be nicer to generate a subclass and use a `TestInstanceFactory` to instantiate that.
    // XXX: That turns out not to work.
    //
    // ^ Other option: provide super class that declares a `TestFactory` based on the subclass name.

  }

  // Extensions can be defined at any level. TBD how useful.
  //  static final class MyExtension implements Extension,  {
//
//  }
//  @ExtendWith(MyExtension.class)

  static class MySubTest extends MyTest {}

  @TestFactory
  Stream<DynamicTest> dynamicTestsFromStreamInJava8() {

    Function<String, String> resolver = s -> s;

    List<String> domainNames =
        Arrays.asList("www.somedomain.com", "www.anotherdomain.com", "www.yetanotherdomain.com");
    List<String> outputList = Arrays.asList("154.174.10.56", "211.152.104.132", "178.144.120.156");

    return domainNames.stream()
        .map(
            dom ->
                DynamicTest.dynamicTest(
                    "Resolving: " + dom,
                    () -> {
                      int id = domainNames.indexOf(dom);

                      assertEquals(outputList.get(id), resolver.apply(dom));
                    }));
  }

  static final class PrimitiveOrReferenceEquality
      implements ExpressionTestCase<ImmutableSet<Boolean>> {
    public ImmutableSet<Boolean> before() {
      return ImmutableSet.of(
          RoundingMode.UP.equals(RoundingMode.DOWN),
          Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
          !RoundingMode.UP.equals(RoundingMode.DOWN),
          !Objects.equals(RoundingMode.UP, RoundingMode.DOWN));
    }

    public ImmutableSet<Boolean> after() {
      return ImmutableSet.of(
          RoundingMode.UP == RoundingMode.DOWN,
          RoundingMode.UP == RoundingMode.DOWN,
          RoundingMode.UP != RoundingMode.DOWN,
          RoundingMode.UP != RoundingMode.DOWN);
    }
  }

  // XXX: This variant can verify that the values are equal.
  static final class PrimitiveOrReferenceEquality2 implements ExpressionTestCase2<Boolean> {
    public void before(Consumer<Boolean> sink) {
      sink.accept(RoundingMode.UP.equals(RoundingMode.DOWN));
      sink.accept(Objects.equals(RoundingMode.UP, RoundingMode.DOWN));
      sink.accept(!RoundingMode.UP.equals(RoundingMode.DOWN));
      sink.accept(!Objects.equals(RoundingMode.UP, RoundingMode.DOWN));
    }

    public void after(Consumer<Boolean> sink) {
      sink.accept(RoundingMode.UP == RoundingMode.DOWN);
      sink.accept(RoundingMode.UP == RoundingMode.DOWN);
      sink.accept(RoundingMode.UP != RoundingMode.DOWN);
      sink.accept(RoundingMode.UP != RoundingMode.DOWN);
    }
  }

  // With this setup we could validate equality, and (in theory) even report the exact source in
  // case of an error.
  //  @Test
  void primitiveOrReferenceEquality(BiConsumer<Boolean, Boolean> testCase) {
    testCase.accept(
        RoundingMode.UP.equals(RoundingMode.DOWN), RoundingMode.UP == RoundingMode.DOWN);
    testCase.accept(
        Objects.equals(RoundingMode.UP, RoundingMode.DOWN), RoundingMode.UP == RoundingMode.DOWN);
    testCase.accept(
        !RoundingMode.UP.equals(RoundingMode.DOWN), RoundingMode.UP != RoundingMode.DOWN);
    testCase.accept(
        !Objects.equals(RoundingMode.UP, RoundingMode.DOWN), RoundingMode.UP != RoundingMode.DOWN);
  }




    // With this setup we could validate equality, and (in theory) even report the exact source in
  // case of an error.
  //  @Test
  void primitiveOrReferenceEquality2(BiConsumer<Supplier<Boolean>, Supplier<Boolean>> testCase) {
    testCase.accept(
        () -> RoundingMode.UP.equals(RoundingMode.DOWN),
        () -> RoundingMode.UP == RoundingMode.DOWN);
    testCase.accept(
        () -> Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
        () -> RoundingMode.UP == RoundingMode.DOWN);
    testCase.accept(
        () -> !RoundingMode.UP.equals(RoundingMode.DOWN),
        () -> RoundingMode.UP != RoundingMode.DOWN);
    testCase.accept(
        () -> !Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
        () -> RoundingMode.UP != RoundingMode.DOWN);
  }

  interface ExpressionTestCase<T> {
    T before();

    T after();
  }

  interface ExpressionTestCase2<T> {
    void before(Consumer<T> sink);

    void after(Consumer<T> sink);
  }

  //  record ExpressionTestCaseX<T>(Supplier<T> before, Supplier<T> after);
}
