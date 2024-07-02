package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ImmutablesSortedSetComparatorTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ImmutablesSortedSetComparator.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.common.collect.ContiguousSet;
            import com.google.common.collect.ImmutableSet;
            import com.google.common.collect.ImmutableSortedSet;
            import java.util.NavigableSet;
            import java.util.Set;
            import java.util.SortedSet;
            import java.util.TreeSet;
            import org.immutables.value.Value;

            interface A {
              @Value.Immutable
              interface ImmutableInterface {
                Set<String> set();

                // BUG: Diagnostic contains:
                SortedSet<String> sortedSet();

                @Value.NaturalOrder
                SortedSet<String> sortedSet2();
              }

              @Value.Modifiable
              interface ModifiableInterfaceWithDefaults {
                @Value.Default
                default Set<Integer> set() {
                  return new TreeSet<>();
                }

                @Value.Default
                // BUG: Diagnostic contains:
                default NavigableSet<Integer> navigableSet() {
                  return new TreeSet<>();
                }

                @Value.Default
                @Value.ReverseOrder
                default NavigableSet<Integer> navigableSet2() {
                  return new TreeSet<>();
                }

                default NavigableSet<Integer> nonPropertyNavigableSet() {
                  return new TreeSet<>();
                }
              }

              interface NonImmutablesInterface {
                SortedSet<String> sortedSet();
              }

              @Value.Immutable
              abstract class AbstractImmutableWithDefaults {
                @Value.Default
                ImmutableSet<Integer> immutableSet() {
                  return ImmutableSet.of();
                }

                @Value.Default
                // BUG: Diagnostic contains:
                ImmutableSortedSet<String> immutableSortedSet() {
                  return ImmutableSortedSet.of();
                }

                @Value.Default
                @Value.NaturalOrder
                ImmutableSortedSet<String> immutableSortedSet2() {
                  return ImmutableSortedSet.of();
                }

                ImmutableSortedSet<String> nonPropertyImmutableSortedSet() {
                  return ImmutableSortedSet.of();
                }
              }

              @Value.Modifiable
              abstract class AbstractModifiable {
                abstract ImmutableSet<Integer> immutableSet();

                // BUG: Diagnostic contains:
                abstract ContiguousSet<Integer> contiguousSet();

                @Value.ReverseOrder
                abstract ContiguousSet<Integer> contiguousSet2();
              }

              abstract class AbstractNonImmutables {
                abstract SortedSet<Integer> sortedSet();
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ImmutablesSortedSetComparator.class, getClass())
        .addInputLines(
            "A.java",
            """
            import com.google.common.collect.ImmutableSortedSet;
            import java.util.SortedSet;
            import org.immutables.value.Value;

            @Value.Immutable
            abstract class A {
              abstract ImmutableSortedSet<String> sortedSet();

              @Value.Modifiable
              interface B {
                SortedSet<String> sortedSet();
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import com.google.common.collect.ImmutableSortedSet;
            import java.util.SortedSet;
            import org.immutables.value.Value;

            @Value.Immutable
            abstract class A {
              @Value.NaturalOrder
              abstract ImmutableSortedSet<String> sortedSet();

              @Value.Modifiable
              interface B {
                @Value.NaturalOrder
                SortedSet<String> sortedSet();
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithImportClash() {
    BugCheckerRefactoringTestHelper.newInstance(ImmutablesSortedSetComparator.class, getClass())
        .addInputLines(
            "MySpringService.java",
            """
            import com.google.common.collect.ImmutableSortedSet;
            import org.springframework.beans.factory.annotation.Value;

            class MySpringService {
              MySpringService(@Value("${someProperty}") String prop) {}
              ;

              @org.immutables.value.Value.Immutable
              interface A {
                ImmutableSortedSet<String> sortedSet();
              }
            }
            """)
        .addOutputLines(
            "MySpringService.java",
            """
            import com.google.common.collect.ImmutableSortedSet;
            import org.springframework.beans.factory.annotation.Value;

            class MySpringService {
              MySpringService(@Value("${someProperty}") String prop) {}
              ;

              @org.immutables.value.Value.Immutable
              interface A {
                @org.immutables.value.Value.NaturalOrder
                ImmutableSortedSet<String> sortedSet();
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
