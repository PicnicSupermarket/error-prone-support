package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class RequestParameterAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RequestParameterAnnotationCheck.class, getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PatchMapping;",
            "import org.springframework.web.bind.annotation.PathVariable;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestMapping;",
            "import org.springframework.web.bind.annotation.RequestMethod;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "",
            "interface A {",
            "  A properNoMapping();",
            "  A properNoMapping(String param);",
            "  @GetMapping A properNoParameters();",
            "  @PostMapping A properRequestBody(@RequestBody String body);",
            "  @DeleteMapping A properPathVariable(@PathVariable String param);",
            "  @PutMapping A properRequestParam(@RequestParam String param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @DeleteMapping A delete(String param);",
            "  // BUG: Diagnostic contains:",
            "  @GetMapping A get(String param);",
            "  // BUG: Diagnostic contains:",
            "  @PatchMapping A patch(String param);",
            "  // BUG: Diagnostic contains:",
            "  @PostMapping A post(String param);",
            "  // BUG: Diagnostic contains:",
            "  @PutMapping A put(String param);",
            "  // BUG: Diagnostic contains:",
            "  @PostMapping A multiple1(String param, String param2);",
            "  // BUG: Diagnostic contains:",
            "  @PostMapping A multiple2(String param, @RequestBody String param2);",
            "  // BUG: Diagnostic contains:",
            "  @PostMapping A multiple3(@RequestBody String param, String param2);",
            "}")
        .doTest();
  }
}
