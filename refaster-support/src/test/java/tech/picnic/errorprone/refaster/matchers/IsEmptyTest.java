package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsEmptyTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableSet;
            import com.google.common.collect.ImmutableSetMultimap;
            import java.util.ArrayList;
            import java.util.Collections;
            import java.util.Comparator;
            import java.util.HashMap;
            import java.util.HashSet;
            import java.util.LinkedHashMap;
            import java.util.LinkedHashSet;
            import java.util.LinkedList;
            import java.util.List;
            import java.util.Map;
            import java.util.Random;
            import java.util.Set;
            import java.util.Stack;
            import java.util.TreeMap;
            import java.util.TreeSet;
            import java.util.Vector;
            import java.util.stream.Stream;
            import reactor.core.publisher.Flux;
            import reactor.core.publisher.Mono;
            import reactor.util.context.Context;

            class A {
              int[] negative1() {
                return new int[1];
              }

              int[][] negative2() {
                return new int[1][0];
              }

              int[] negative3() {
                return new int[] {0};
              }

              int[][] negative4() {
                return new int[][] {{0}};
              }

              int[] negative5() {
                int i = hashCode();
                return new int[i];
              }

              Random negative6() {
                return new Random();
              }

              List<Integer> negative7() {
                return new ArrayList<>(ImmutableList.of(1));
              }

              Map<Integer, Integer> negative8() {
                return new HashMap<>(ImmutableMap.of(1, 2));
              }

              Set<Integer> negative9() {
                return new HashSet<>(ImmutableList.of(1));
              }

              Map<Integer, Integer> negative10() {
                return new LinkedHashMap<>(ImmutableMap.of(1, 2));
              }

              Set<Integer> negative11() {
                return new LinkedHashSet<>(ImmutableList.of(1));
              }

              List<Integer> negative12() {
                return new LinkedList<>(ImmutableList.of(1));
              }

              Map<Integer, Integer> negative13() {
                return new HashMap<>(ImmutableMap.of(1, 2));
              }

              Set<Integer> negative14() {
                return new HashSet<>(ImmutableList.of(1));
              }

              List<Integer> negative15() {
                return new Vector<>(ImmutableList.of(1));
              }

              ImmutableList<Integer> negative16() {
                return ImmutableList.of(1);
              }

              ImmutableSet<Integer> negative17() {
                return ImmutableSet.of(1);
              }

              ImmutableMap<Integer, Integer> negative18() {
                return ImmutableMap.of(1, 2);
              }

              ImmutableSetMultimap<Integer, Integer> negative19() {
                return ImmutableSetMultimap.of(1, 2);
              }

              List<Integer> negative20() {
                return List.of(1);
              }

              Map<Integer, Integer> negative21() {
                return Map.of(1, 2);
              }

              Set<Integer> negative22() {
                return Set.of(1);
              }

              Stream<Integer> negative23() {
                return Stream.of(1);
              }

              int[] positive1() {
                // BUG: Diagnostic contains:
                return new int[0];
              }

              int[][] positive2() {
                // BUG: Diagnostic contains:
                return new int[0][1];
              }

              int[] positive3() {
                // BUG: Diagnostic contains:
                return new int[] {};
              }

              int[][] positive4() {
                // BUG: Diagnostic contains:
                return new int[][] {};
              }

              List<Integer> positive5() {
                // BUG: Diagnostic contains:
                return new ArrayList<>();
              }

              List<String> positive6() {
                // BUG: Diagnostic contains:
                return new ArrayList<>(1);
              }

              List<String> positive7() {
                // BUG: Diagnostic contains:
                return new ArrayList<>(
                    // BUG: Diagnostic contains:
                    ImmutableList.of());
              }

              Map<String, String> positive8() {
                // BUG: Diagnostic contains:
                return new HashMap<>();
              }

              Map<String, String> positive9() {
                // BUG: Diagnostic contains:
                return new HashMap<>(1);
              }

              Map<String, String> positive10() {
                // BUG: Diagnostic contains:
                return new HashMap<>(1, 1.0F);
              }

              Map<String, String> positive11() {
                // BUG: Diagnostic contains:
                return new HashMap<>(
                    // BUG: Diagnostic contains:
                    ImmutableMap.of());
              }

              Set<Integer> positive12() {
                // BUG: Diagnostic contains:
                return new HashSet<>();
              }

              Set<Integer> positive13() {
                // BUG: Diagnostic contains:
                return new HashSet<>(1);
              }

              Set<Integer> positive14() {
                // BUG: Diagnostic contains:
                return new HashSet<>(1, 1.0F);
              }

              Set<Integer> positive15() {
                // BUG: Diagnostic contains:
                return new HashSet<>(
                    // BUG: Diagnostic contains:
                    ImmutableList.of());
              }

              Map<String, String> positive16() {
                // BUG: Diagnostic contains:
                return new LinkedHashMap<>();
              }

              Map<String, String> positive17() {
                // BUG: Diagnostic contains:
                return new LinkedHashMap<>(1);
              }

              Map<String, String> positive18() {
                // BUG: Diagnostic contains:
                return new LinkedHashMap<>(1, 1.0F);
              }

              Map<String, String> positive19() {
                // BUG: Diagnostic contains:
                return new LinkedHashMap<>(1, 1.0F, false);
              }

              Map<String, String> positive20() {
                // BUG: Diagnostic contains:
                return new LinkedHashMap<>(
                    // BUG: Diagnostic contains:
                    ImmutableMap.of());
              }

              Set<Integer> positive21() {
                // BUG: Diagnostic contains:
                return new LinkedHashSet<>();
              }

              Set<Integer> positive22() {
                // BUG: Diagnostic contains:
                return new LinkedHashSet<>(1);
              }

              Set<Integer> positive23() {
                // BUG: Diagnostic contains:
                return new LinkedHashSet<>(1, 1.0F);
              }

              Set<Integer> positive24() {
                // BUG: Diagnostic contains:
                return new LinkedHashSet<>(
                    // BUG: Diagnostic contains:
                    ImmutableList.of());
              }

              List<Integer> positive25() {
                // BUG: Diagnostic contains:
                return new LinkedList<>();
              }

              List<String> positive26() {
                // BUG: Diagnostic contains:
                return new LinkedList<>(
                    // BUG: Diagnostic contains:
                    ImmutableList.of());
              }

              List<Integer> positive27() {
                // BUG: Diagnostic contains:
                return new Stack<>();
              }

              Map<String, String> positive28() {
                // BUG: Diagnostic contains:
                return new TreeMap<>();
              }

              Map<String, String> positive29() {
                // BUG: Diagnostic contains:
                return new TreeMap<>(Comparator.naturalOrder());
              }

              Map<String, String> positive30() {
                // BUG: Diagnostic contains:
                return new TreeMap<>(
                    // BUG: Diagnostic contains:
                    ImmutableMap.of());
              }

              Set<Integer> positive31() {
                // BUG: Diagnostic contains:
                return new TreeSet<>();
              }

              Set<Integer> positive32() {
                // BUG: Diagnostic contains:
                return new TreeSet<>(Comparator.naturalOrder());
              }

              Set<Integer> positive33() {
                // BUG: Diagnostic contains:
                return new TreeSet<>(
                    // BUG: Diagnostic contains:
                    ImmutableList.of());
              }

              List<Integer> positive34() {
                // BUG: Diagnostic contains:
                return new Vector<>();
              }

              List<Integer> positive35() {
                // BUG: Diagnostic contains:
                return new Vector<>(1);
              }

              List<Integer> positive36() {
                // BUG: Diagnostic contains:
                return new Vector<>(1, 2);
              }

              List<Integer> positive37() {
                // BUG: Diagnostic contains:
                return new Vector<>(
                    // BUG: Diagnostic contains:
                    ImmutableList.of());
              }

              List<Integer> positive38() {
                // BUG: Diagnostic contains:
                return Collections.EMPTY_LIST;
              }

              Map<String, String> positive39() {
                // BUG: Diagnostic contains:
                return Collections.EMPTY_MAP;
              }

              Set<Integer> positive40() {
                // BUG: Diagnostic contains:
                return Collections.EMPTY_SET;
              }

              List<Integer> positive41() {
                // BUG: Diagnostic contains:
                return Collections.emptyList();
              }

              Map<String, String> positive42() {
                // BUG: Diagnostic contains:
                return Collections.emptyMap();
              }

              Set<Integer> positive43() {
                // BUG: Diagnostic contains:
                return Collections.emptySet();
              }

              ImmutableList<Integer> positive44() {
                // BUG: Diagnostic contains:
                return ImmutableList.of();
              }

              ImmutableSet<Integer> positive45() {
                // BUG: Diagnostic contains:
                return ImmutableSet.of();
              }

              ImmutableMap<String, Integer> positive46() {
                // BUG: Diagnostic contains:
                return ImmutableMap.of();
              }

              ImmutableSetMultimap<String, Integer> positive47() {
                // BUG: Diagnostic contains:
                return ImmutableSetMultimap.of();
              }

              List<Integer> positive48() {
                // BUG: Diagnostic contains:
                return List.of();
              }

              Map<String, String> positive49() {
                // BUG: Diagnostic contains:
                return Map.of();
              }

              Set<Integer> positive50() {
                // BUG: Diagnostic contains:
                return Set.of();
              }

              Stream<Integer> positive51() {
                // BUG: Diagnostic contains:
                return Stream.of();
              }

              Stream<Integer> positive52() {
                // BUG: Diagnostic contains:
                return Stream.empty();
              }

              Flux<Integer> positive53() {
                // BUG: Diagnostic contains:
                return Flux.empty();
              }

              Mono<Integer> positive54() {
                // BUG: Diagnostic contains:
                return Mono.empty();
              }

              Context positive55() {
                // BUG: Diagnostic contains:
                return Context.empty();
              }

              Flux<Integer> positive56() {
                // BUG: Diagnostic contains:
                return Flux.just();
              }
            }
            """)
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsEmpty}. */
  @BugPattern(summary = "Flags expressions matched by `IsEmpty`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsEmpty());
    }
  }
}
