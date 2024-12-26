package tech.picnic.errorprone.guidelines.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ExhaustiveRefasterTypeMigrationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ExhaustiveRefasterTypeMigration.class, getClass())
        .addSourceLines(
            "Util.java",
            """
            class Util {
              public static int CONSTANT = 42;

              public static void publicStaticVoidMethod() {}

              static void packagePrivateStaticVoidMethod() {}

              protected static void protectedStaticVoidMethod() {}

              private static void privateStaticVoidMethod() {}

              public static int publicStaticIntMethod2() {
                return 0;
              }

              public String publicStringMethodWithArg(int arg) {
                return String.valueOf(arg);
              }
            }
            """)
        .addSourceLines(
            "A.java",
            """
            import com.google.errorprone.refaster.annotation.AfterTemplate;
            import com.google.errorprone.refaster.annotation.BeforeTemplate;
            import tech.picnic.errorprone.refaster.annotation.TypeMigration;

            class A {
              class UnannotatedEmptyClass {}

              // BUG: Diagnostic contains: Migration of type 'int' is unsupported
              @TypeMigration(of = int.class)
              class AnnotatedWithPrimitive {}

              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {
                    "publicStaticIntMethod2()",
                    "publicStringMethodWithArg(int)",
                    "publicStaticVoidMethod()"
                  })
              class AnnotatedEmptyClass {}

              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {
                    "publicStaticVoidMethod()",
                    "publicStringMethodWithArg(int)",
                    "publicStaticIntMethod2()"
                  })
              class AnnotatedEmptyClassWithUnsortedMethodListing {}

              class UnannotatedTemplate {
                @BeforeTemplate
                void before(int value) {
                  Util.publicStaticVoidMethod();
                  Util.publicStaticIntMethod2();
                  new Util().publicStringMethodWithArg(value);
                }
              }

              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {
                    "publicStaticIntMethod2()",
                    "publicStringMethodWithArg(int)",
                    "publicStaticVoidMethod()"
                  })
              class AnnotatedWithoutBeforeTemplate {
                {
                  Util.publicStaticIntMethod2();
                }

                @AfterTemplate
                void after(int value) {
                  Util.publicStaticVoidMethod();
                  new Util().publicStringMethodWithArg(value);
                }
              }

              @TypeMigration(of = Util.class)
              class AnnotatedFullyMigrated {
                @BeforeTemplate
                void before() {
                  new Util().publicStringMethodWithArg(Util.publicStaticIntMethod2());
                }

                @BeforeTemplate
                void before2() {
                  Util.publicStaticVoidMethod();
                }
              }

              @TypeMigration(of = Util.class, unmigratedMethods = "publicStringMethodWithArg(int)")
              class AnnotatedPartiallyMigrated {
                @BeforeTemplate
                void before() {
                  Util.publicStaticVoidMethod();
                  Util.publicStaticIntMethod2();
                }
              }

              // BUG: Diagnostic contains: The set of unmigrated methods listed by the `@TypeMigration`
              // annotation must be minimal yet exhaustive
              @TypeMigration(of = Util.class, unmigratedMethods = "publicStringMethodWithArg(int)")
              class AnnotatedWithIncompleteMethodListing {
                @BeforeTemplate
                void before() {
                  Util.publicStaticIntMethod2();
                }
              }

              // BUG: Diagnostic contains: The set of unmigrated methods listed by the `@TypeMigration`
              // annotation must be minimal yet exhaustive
              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {"publicStaticIntMethod2()", "publicStringMethodWithArg(int)"})
              class AnnotatedWithMigratedMethodReference {
                @BeforeTemplate
                void before() {
                  Util.publicStaticVoidMethod();
                  Util.publicStaticIntMethod2();
                }
              }

              // BUG: Diagnostic contains: The set of unmigrated methods listed by the `@TypeMigration`
              // annotation must be minimal yet exhaustive
              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {"extra", "publicStringMethodWithArg(int)"})
              class AnnotatedWithUnknownMethodReference {
                @BeforeTemplate
                void before() {
                  Util.publicStaticVoidMethod();
                  Util.publicStaticIntMethod2();
                }
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ExhaustiveRefasterTypeMigration.class, getClass())
        .addInputLines(
            "Util.java",
            """
            public final class Util {
              public static void publicStaticVoidMethod() {}

              public static int publicStaticIntMethod2() {
                return 0;
              }

              public String publicStringMethodWithArg(int arg) {
                return String.valueOf(arg);
              }

              public String publicStringMethodWithArg(String arg) {
                return arg;
              }
            }
            """)
        .expectUnchanged()
        .addInputLines(
            "A.java",
            """
            import com.google.errorprone.refaster.annotation.BeforeTemplate;
            import tech.picnic.errorprone.refaster.annotation.TypeMigration;

            class A {
              @TypeMigration(of = Util.class)
              class AnnotatedWithoutMethodListing {
                {
                  new Util().publicStringMethodWithArg(1);
                }

                @BeforeTemplate
                void before() {
                  Util.publicStaticIntMethod2();
                }
              }

              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {"publicStaticIntMethod2()", "extra", "publicStringMethodWithArg(int)"})
              class AnnotatedWithIncorrectMethodReference {
                @BeforeTemplate
                void before() {
                  new Util().publicStringMethodWithArg("1");
                  Util.publicStaticVoidMethod();
                  Util.publicStaticIntMethod2();
                }
              }

              @TypeMigration(
                  of = Util.class,
                  unmigratedMethods = {"publicStaticVoidMethod()", "publicStaticVoidMethod()"})
              class AnnotatedWithDuplicateMethodReference {
                @BeforeTemplate
                void before() {
                  new Util().publicStringMethodWithArg(1);
                  new Util().publicStringMethodWithArg("1");
                  Util.publicStaticIntMethod2();
                }
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import com.google.errorprone.refaster.annotation.BeforeTemplate;
            import tech.picnic.errorprone.refaster.annotation.TypeMigration;

            class A {
              @TypeMigration(
                  unmigratedMethods = {
                    "publicStaticVoidMethod()",
                    "publicStringMethodWithArg(int)",
                    "publicStringMethodWithArg(String)",
                    "Util()"
                  },
                  of = Util.class)
              class AnnotatedWithoutMethodListing {
                {
                  new Util().publicStringMethodWithArg(1);
                }

                @BeforeTemplate
                void before() {
                  Util.publicStaticIntMethod2();
                }
              }

              @TypeMigration(of = Util.class, unmigratedMethods = "publicStringMethodWithArg(int)")
              class AnnotatedWithIncorrectMethodReference {
                @BeforeTemplate
                void before() {
                  new Util().publicStringMethodWithArg("1");
                  Util.publicStaticVoidMethod();
                  Util.publicStaticIntMethod2();
                }
              }

              @TypeMigration(of = Util.class, unmigratedMethods = "publicStaticVoidMethod()")
              class AnnotatedWithDuplicateMethodReference {
                @BeforeTemplate
                void before() {
                  new Util().publicStringMethodWithArg(1);
                  new Util().publicStringMethodWithArg("1");
                  Util.publicStaticIntMethod2();
                }
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
