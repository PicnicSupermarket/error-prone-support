class A {
  class AInner2 {
    private static final int foo = 1;
    int bar = 2;

    class AInner2Inner1 {
      private static final int foo = 1;
      int bar = 2;
    }

    class AInner2Inner2 {
      int bar = 2;
      private static final int foo = 1;
    }
  }

  private static final int foo = 1;

  class OnlyTopLevelSortedClass {
    private static final int foo = 1;
    int bar = 2;

    class UnsortedNestedClass {
      int bar = 2;
      private static final int foo = 1;
    }
  }

  class AInner1 {
    class AInner1Inner1 {
      int bar = 2;
      private static final int foo = 1;
    }

    enum DeeplyNestedEnum {
      /** FOO's JavaDoc */
      FOO,
    /* Dangling comment trailing enumerations. */ ;

      /** `quz` method's dangling comment */
      ;

      /** `quz` method's comment */
      void qux() {}

      // `baz` method's comment
      final int baz = 2;

      static final int BAR = 1;
      // trailing comment
    }

    private static final int foo = 1;

    class AInner1Inner2 {
      int bar = 2;
      private static final int foo = 1;
    }

    int bar = 2;
  }

  static int baz = 3;
  private final int bar = 2;
}
