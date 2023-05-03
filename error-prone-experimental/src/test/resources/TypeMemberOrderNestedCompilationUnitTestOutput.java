class A {

  private static final int foo = 1;

  static int baz = 3;
  private final int bar = 2;

  class AInner2 {
    private static final int foo = 1;
    int bar = 2;

    class AInner2Inner1 {
      private static final int foo = 1;
      int bar = 2;
    }

    class AInner2Inner2 {
      private static final int foo = 1;
      int bar = 2;
    }
  }

  class OnlyTopLevelSortedClass {
    private static final int foo = 1;
    int bar = 2;

    class UnsortedNestedClass {
      private static final int foo = 1;
      int bar = 2;
    }
  }

  class AInner1 {

    private static final int foo = 1;

    int bar = 2;

    class AInner1Inner1 {
      private static final int foo = 1;
      int bar = 2;
    }

    enum DeeplyNestedEnum {
      /** FOO's JavaDoc */
      FOO,
    /* Dangling comment trailing enumerations. */ ;

      static final int BAR = 1;

      // `baz` method's comment
      final int baz = 2;

      /** `quz` method's dangling comment */
      ;

      /** `quz` method's comment */
      void qux() {}
      // trailing comment
    }

    class AInner1Inner2 {
      private static final int foo = 1;
      int bar = 2;
    }
  }
}
