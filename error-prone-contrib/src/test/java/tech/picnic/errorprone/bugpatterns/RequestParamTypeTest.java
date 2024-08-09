package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RequestParamTypeTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(RequestParamType.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.common.collect.ImmutableBiMap;
            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableSet;
            import java.util.List;
            import java.util.Map;
            import java.util.Set;
            import org.jspecify.annotations.Nullable;
            import org.springframework.web.bind.annotation.DeleteMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.PutMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestParam;

            interface A {
              @PostMapping
              A properRequestParam(@RequestBody String body);

              @GetMapping
              A properRequestParam(@RequestParam int param);

              @GetMapping
              A properRequestParam(@RequestParam List<String> param);

              @PostMapping
              A properRequestParam(@RequestBody String body, @RequestParam Set<String> param);

              @PutMapping
              A properRequestParam(@RequestBody String body, @RequestParam Map<String, String> param);

              @GetMapping
              // BUG: Diagnostic contains:
              A get(@RequestParam ImmutableBiMap<String, String> param);

              @PostMapping
              // BUG: Diagnostic contains:
              A post(@Nullable @RequestParam ImmutableList<String> param);

              @PutMapping
              // BUG: Diagnostic contains:
              A put(@RequestBody String body, @RequestParam ImmutableSet<String> param);

              @DeleteMapping
              // BUG: Diagnostic contains:
              A delete(@RequestBody String body, @RequestParam ImmutableMap<String, String> param);

              void negative(ImmutableSet<Integer> set, ImmutableMap<String, String> map);
            }
            """)
        .doTest();
  }

  @Test
  void identificationRestricted() {
    CompilationTestHelper.newInstance(RequestParamType.class, getClass())
        .setArgs(
            "-XepOpt:RequestParamType:SupportedCustomTypes=com.google.common.collect.ImmutableSet,com.google.common.collect.ImmutableSortedMultiset")
        .addSourceLines(
            "A.java",
            """
            import com.google.common.collect.ImmutableBiMap;
            import com.google.common.collect.ImmutableCollection;
            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableMultiset;
            import com.google.common.collect.ImmutableSet;
            import com.google.common.collect.ImmutableSortedMultiset;
            import com.google.common.collect.ImmutableSortedSet;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RequestParam;

            interface A {
              @GetMapping
              // BUG: Diagnostic contains:
              A immutableCollection(@RequestParam ImmutableCollection<String> param);

              @GetMapping
              // BUG: Diagnostic contains:
              A immutableList(@RequestParam ImmutableList<String> param);

              @GetMapping
              A immutableSet(@RequestParam ImmutableSet<String> param);

              @GetMapping
              A immutableSortedSet(@RequestParam ImmutableSortedSet<String> param);

              @GetMapping
              // BUG: Diagnostic contains:
              A immutableMultiset(@RequestParam ImmutableMultiset<String> param);

              @GetMapping
              A immutableSortedMultiset(@RequestParam ImmutableSortedMultiset<String> param);

              @GetMapping
              // BUG: Diagnostic contains:
              A immutableMap(@RequestParam ImmutableMap<String, String> param);

              @GetMapping
              // BUG: Diagnostic contains:
              A immutableBiMap(@RequestParam ImmutableBiMap<String, String> param);
            }
            """)
        .doTest();
  }
}
