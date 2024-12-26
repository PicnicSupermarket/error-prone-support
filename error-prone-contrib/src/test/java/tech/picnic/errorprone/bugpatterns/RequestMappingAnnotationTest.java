package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RequestMappingAnnotationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(RequestMappingAnnotation.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import jakarta.servlet.http.HttpServletRequest;
            import jakarta.servlet.http.HttpServletResponse;
            import java.io.InputStream;
            import java.time.ZoneId;
            import java.util.Locale;
            import java.util.TimeZone;
            import org.springframework.http.HttpMethod;
            import org.springframework.security.core.annotation.CurrentSecurityContext;
            import org.springframework.ui.Model;
            import org.springframework.validation.BindingResult;
            import org.springframework.web.bind.annotation.DeleteMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PatchMapping;
            import org.springframework.web.bind.annotation.PathVariable;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.PutMapping;
            import org.springframework.web.bind.annotation.RequestAttribute;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestHeader;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RequestParam;
            import org.springframework.web.bind.annotation.RequestPart;
            import org.springframework.web.context.request.NativeWebRequest;
            import org.springframework.web.context.request.WebRequest;
            import org.springframework.web.server.ServerWebExchange;
            import org.springframework.web.util.UriBuilder;
            import org.springframework.web.util.UriComponentsBuilder;

            interface A {
              A noMapping();

              A noMapping(String param);

              @DeleteMapping
              A properNoParameters();

              @GetMapping
              A properPathVariable(@PathVariable String param);

              @PatchMapping
              A properRequestAttribute(@RequestAttribute String attribute);

              @PostMapping
              A properRequestBody(@RequestBody String body);

              @PutMapping
              A properRequestHeader(@RequestHeader String header);

              @RequestMapping
              A properRequestParam(@RequestParam String param);

              @RequestMapping
              A properRequestPart(@RequestPart String part);

              @RequestMapping
              A properCurrentSecurityContext(
                  @CurrentSecurityContext(expression = "authentication.name") String user);

              @RequestMapping
              A properInputStream(InputStream input);

              @RequestMapping
              A properZoneId(ZoneId zoneId);

              @RequestMapping
              A properLocale(Locale locale);

              @RequestMapping
              A properTimeZone(TimeZone timeZone);

              @RequestMapping
              A properHttpServletRequest(HttpServletRequest request);

              @RequestMapping
              A properHttpServletResponse(HttpServletResponse response);

              @RequestMapping
              A properHttpMethod(HttpMethod method);

              @RequestMapping
              A properModel(Model model);

              @RequestMapping
              A properBindingResult(BindingResult result);

              @RequestMapping
              A properNativeWebRequest(NativeWebRequest request);

              @RequestMapping
              A properWebRequest(WebRequest request);

              @RequestMapping
              A properServerWebExchange(ServerWebExchange exchange);

              @RequestMapping
              A properServerUriBuilder(UriBuilder builder);

              @RequestMapping
              A properServerUriComponentsBuilder(UriComponentsBuilder builder);

              @DeleteMapping
              // BUG: Diagnostic contains:
              A delete(String param);

              @GetMapping
              // BUG: Diagnostic contains:
              A get(String param);

              @PatchMapping
              // BUG: Diagnostic contains:
              A patch(String param);

              @PostMapping
              // BUG: Diagnostic contains:
              A post(String param);

              @PutMapping
              // BUG: Diagnostic contains:
              A put(String param);

              @RequestMapping
              // BUG: Diagnostic contains:
              A requestMultiple(String param, String param2);

              @RequestMapping
              // BUG: Diagnostic contains:
              A requestFirstParamViolation(String param, @PathVariable String param2);

              @RequestMapping
              // BUG: Diagnostic contains:
              A requestSecondParamViolation(@RequestBody String param, String param2);
            }
            """)
        .doTest();
  }
}
