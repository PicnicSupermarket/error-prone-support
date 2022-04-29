package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RequestMappingAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RequestMappingAnnotationCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.io.InputStream;",
            "import java.time.ZoneId;",
            "import java.util.Locale;",
            "import java.util.TimeZone;",
            "import javax.servlet.http.HttpServletRequest;",
            "import javax.servlet.http.HttpServletResponse;",
            "import org.springframework.http.HttpMethod;",
            "import org.springframework.web.bind.annotation.DeleteMapping;",
            "import org.springframework.web.bind.annotation.GetMapping;",
            "import org.springframework.web.bind.annotation.PatchMapping;",
            "import org.springframework.web.bind.annotation.PathVariable;",
            "import org.springframework.web.bind.annotation.PostMapping;",
            "import org.springframework.web.bind.annotation.PutMapping;",
            "import org.springframework.web.bind.annotation.RequestBody;",
            "import org.springframework.web.bind.annotation.RequestHeader;",
            "import org.springframework.web.bind.annotation.RequestMapping;",
            "import org.springframework.web.bind.annotation.RequestMethod;",
            "import org.springframework.web.bind.annotation.RequestParam;",
            "import org.springframework.web.context.request.NativeWebRequest;",
            "import org.springframework.web.context.request.WebRequest;",
            "import org.springframework.web.server.ServerWebExchange;",
            "import org.springframework.web.util.UriBuilder;",
            "import org.springframework.web.util.UriComponentsBuilder;",
            "",
            "interface A {",
            "  A noMapping();",
            "  A noMapping(String param);",
            "  @DeleteMapping A properNoParameters();",
            "  @GetMapping A properPathVariable(@PathVariable String param);",
            "  @PatchMapping A properRequestBody(@RequestBody String body);",
            "  @PostMapping A properRequestHeader(@RequestHeader String header);",
            "  @PutMapping A properRequestParam(@RequestParam String param);",
            "  @RequestMapping A properInputStream(InputStream input);",
            "  @RequestMapping A properZoneId(ZoneId zoneId);",
            "  @RequestMapping A properLocale(Locale locale);",
            "  @RequestMapping A properTimeZone(TimeZone timeZone);",
            "  @RequestMapping A properHttpServletRequest(HttpServletRequest request);",
            "  @RequestMapping A properHttpServletResponse(HttpServletResponse response);",
            "  @RequestMapping A properHttpMethod(HttpMethod method);",
            "  @RequestMapping A properNativeWebRequest(NativeWebRequest request);",
            "  @RequestMapping A properWebRequest(WebRequest request);",
            "  @RequestMapping A properServerWebExchange(ServerWebExchange exchange);",
            "  @RequestMapping A properServerUriBuilder(UriBuilder builder);",
            "  @RequestMapping A properServerUriComponentsBuilder(UriComponentsBuilder builder);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @DeleteMapping A delete(String param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @GetMapping A get(String param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @PatchMapping A patch(String param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @PostMapping A post(String param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @PutMapping A put(String param);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @RequestMapping A requestMultiple(String param, String param2);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @RequestMapping A requestFirstParamViolation(String param, @PathVariable String param2);",
            "",
            "  // BUG: Diagnostic contains:",
            "  @RequestMapping A requestSecondParamViolation(@RequestBody String param, String param2);",
            "}")
        .doTest();
  }
}
