package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RequestParamAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RequestParamAnnotationCheck.class, getClass());

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
            "import javax.annotation.Nullable;",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "",
            "interface A {",
            "  @PostMapping A properRequestParam(@RequestBody String body);",
            "  @GetMapping A properRequestParam(@RequestParam int param);",
            "  @GetMapping A properRequestParam(@RequestParam List<String> param);",
            "  @PostMapping A properRequestParam(@RequestBody String body, @RequestParam Set<String> param);",
            "  @PutMapping A properRequestParam(@RequestBody String body, @RequestParam Map<String, String> param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @GetMapping A get(@RequestParam ImmutableBiMap<String, String> param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @PostMapping A post(@Nullable @RequestParam ImmutableList<String> param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @PutMapping A put(@RequestBody String body, @RequestParam ImmutableSet<String> param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @DeleteMapping A delete(@RequestBody String body, @RequestParam ImmutableMap<String, String> param);",
            "",
            "  void negative(ImmutableSet<Integer> set, ImmutableMap<String, String> map);",
            "}")
        .doTest();
  }
}
