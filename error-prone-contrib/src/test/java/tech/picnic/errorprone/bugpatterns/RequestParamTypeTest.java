package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RequestParamTypeTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RequestParamType.class, getClass());
  private final CompilationTestHelper restrictedCompilationTestHelper =
      CompilationTestHelper.newInstance(RequestParamType.class, getClass())
          .setArgs(
              ImmutableList.of(
                  "-XepOpt:RequestParamType:Includes=com.google.common.collect.ImmutableCollection"));
  private final CompilationTestHelper restrictedWithSubTypeCompilationTestHelper =
      CompilationTestHelper.newInstance(RequestParamType.class, getClass())
          .setArgs(
              ImmutableList.of(
                  "-XepOpt:RequestParamType:Includes=com.google.common.collect.ImmutableSet"));
  private final CompilationTestHelper restrictedWithInvalidFlagCompilationTestHelper =
      CompilationTestHelper.newInstance(RequestParamType.class, getClass())
          .setArgs(ImmutableList.of("-XepOpt:RequestParamType:Includes="));

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableBiMap;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.List;",
            "import java.util.Map;",
            "import java.util.Set;",
            "import org.jspecify.annotations.Nullable;",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "",
            "interface A {",
            "  @PostMapping",
            "  A properRequestParam(@RequestBody String body);",
            "",
            "  @GetMapping",
            "  A properRequestParam(@RequestParam int param);",
            "",
            "  @GetMapping",
            "  A properRequestParam(@RequestParam List<String> param);",
            "",
            "  @PostMapping",
            "  A properRequestParam(@RequestBody String body, @RequestParam Set<String> param);",
            "",
            "  @PutMapping",
            "  A properRequestParam(@RequestBody String body, @RequestParam Map<String, String> param);",
            "",
            "  @GetMapping",
            "  // BUG: Diagnostic contains:",
            "  A get(@RequestParam ImmutableBiMap<String, String> param);",
            "",
            "  @PostMapping",
            "  // BUG: Diagnostic contains:",
            "  A post(@Nullable @RequestParam ImmutableList<String> param);",
            "",
            "  @PutMapping",
            "  // BUG: Diagnostic contains:",
            "  A put(@RequestBody String body, @RequestParam ImmutableSet<String> param);",
            "",
            "  @DeleteMapping",
            "  // BUG: Diagnostic contains:",
            "  A delete(@RequestBody String body, @RequestParam ImmutableMap<String, String> param);",
            "",
            "  void negative(ImmutableSet<Integer> set, ImmutableMap<String, String> map);",
            "}")
        .doTest();
  }

  @Test
  void identificationOfIncludedClass() {
    restrictedCompilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableBiMap;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.List;",
            "import java.util.Map;",
            "import java.util.Set;",
            "import org.jspecify.annotations.Nullable;",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "",
            "interface A {",
            "  @PostMapping",
            "  A properRequestParam(@RequestBody String body);",
            "",
            "  @GetMapping",
            "  A properRequestParam(@RequestParam int param);",
            "",
            "  @GetMapping",
            "  A properRequestParam(@RequestParam List<String> param);",
            "",
            "  @PostMapping",
            "  A properRequestParam(@RequestBody String body, @RequestParam Set<String> param);",
            "",
            "  @PutMapping",
            "  A properRequestParam(@RequestBody String body, @RequestParam Map<String, String> param);",
            "",
            "  @GetMapping",
            "  // BUG: Diagnostic contains:",
            "  A get(@RequestParam ImmutableBiMap<String, String> param);",
            "",
            "  @PostMapping",
            "  A post(@Nullable @RequestParam ImmutableList<String> param);",
            "",
            "  @PutMapping",
            "  A put(@RequestBody String body, @RequestParam ImmutableSet<String> param);",
            "",
            "  @DeleteMapping",
            "  // BUG: Diagnostic contains:",
            "  A delete(@RequestBody String body, @RequestParam ImmutableMap<String, String> param);",
            "",
            "  void negative(ImmutableSet<Integer> set, ImmutableMap<String, String> map);",
            "}")
        .doTest();
  }

  @Test
  void identificationOfIncludedSubClass() {
    restrictedWithSubTypeCompilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableBiMap;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import org.jspecify.annotations.Nullable;",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "",
            "interface A {",
            "  @GetMapping",
            "  // BUG: Diagnostic contains:",
            "  A get(@RequestParam ImmutableBiMap<String, String> param);",
            "",
            "  @PostMapping",
            "  // BUG: Diagnostic contains:",
            "  A post(@Nullable @RequestParam ImmutableList<String> param);",
            "",
            "  @PutMapping",
            "  A put(@RequestBody String body, @RequestParam ImmutableSet<String> param);",
            "",
            "  @DeleteMapping",
            "  // BUG: Diagnostic contains:",
            "  A delete(@RequestBody String body, @RequestParam ImmutableMap<String, String> param);",
            "",
            "  void negative(ImmutableSet<Integer> set, ImmutableMap<String, String> map);",
            "}")
        .doTest();
  }

  @Test
  void identificationOfInvalidFlag() {
    restrictedWithInvalidFlagCompilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableBiMap;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.List;",
            "import java.util.Map;",
            "import java.util.Set;",
            "import org.jspecify.annotations.Nullable;",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "",
            "interface A {",
            "  @PostMapping",
            "  A properRequestParam(@RequestBody String body);",
            "",
            "  @GetMapping",
            "  A properRequestParam(@RequestParam int param);",
            "",
            "  @GetMapping",
            "  A properRequestParam(@RequestParam List<String> param);",
            "",
            "  @PostMapping",
            "  A properRequestParam(@RequestBody String body, @RequestParam Set<String> param);",
            "",
            "  @PutMapping",
            "  A properRequestParam(@RequestBody String body, @RequestParam Map<String, String> param);",
            "",
            "  @GetMapping",
            "  // BUG: Diagnostic contains:",
            "  A get(@RequestParam ImmutableBiMap<String, String> param);",
            "",
            "  @PostMapping",
            "  // BUG: Diagnostic contains:",
            "  A post(@Nullable @RequestParam ImmutableList<String> param);",
            "",
            "  @PutMapping",
            "  // BUG: Diagnostic contains:",
            "  A put(@RequestBody String body, @RequestParam ImmutableSet<String> param);",
            "",
            "  @DeleteMapping",
            "  // BUG: Diagnostic contains:",
            "  A delete(@RequestBody String body, @RequestParam ImmutableMap<String, String> param);",
            "",
            "  void negative(ImmutableSet<Integer> set, ImmutableMap<String, String> map);",
            "}")
        .doTest();
  }
}
