package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class SpringMvcAnnotationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(SpringMvcAnnotation.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
            import static org.springframework.web.bind.annotation.RequestMethod.GET;
            import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
            import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
            import static org.springframework.web.bind.annotation.RequestMethod.POST;
            import static org.springframework.web.bind.annotation.RequestMethod.PUT;

            import org.springframework.web.bind.annotation.DeleteMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PatchMapping;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.PutMapping;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RequestMethod;

            interface A {
              @RequestMapping
              A simple();

              @RequestMapping(method = {})
              A explicitDefault();

              // BUG: Diagnostic contains:
              @RequestMapping(method = RequestMethod.GET)
              A get();

              // BUG: Diagnostic contains:
              @RequestMapping(method = {RequestMethod.POST})
              A post();

              // BUG: Diagnostic contains:
              @RequestMapping(method = {PUT})
              A put();

              // BUG: Diagnostic contains:
              @RequestMapping(method = {DELETE})
              A delete();

              // BUG: Diagnostic contains:
              @RequestMapping(method = {PATCH})
              A patch();

              @RequestMapping(method = HEAD)
              A head();

              @RequestMapping(method = RequestMethod.OPTIONS)
              A options();

              @RequestMapping(method = {GET, POST})
              A simpleMix();

              @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
              A verboseMix();

              @DeleteMapping
              A properDelete();

              @GetMapping
              A properGet();

              @PatchMapping
              A properPatch();

              @PostMapping
              A properPost();

              @PutMapping
              A properPut();
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(SpringMvcAnnotation.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
            import static org.springframework.web.bind.annotation.RequestMethod.POST;
            import static org.springframework.web.bind.annotation.RequestMethod.PUT;

            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RequestMethod;

            interface A {
              @RequestMapping(method = RequestMethod.GET)
              A simple();

              @RequestMapping(path = "/foo/bar", method = POST)
              A prefixed();

              @RequestMapping(
                  method = {RequestMethod.DELETE},
                  path = "/foo/bar")
              A suffixed();

              @RequestMapping(
                  path = "/foo/bar",
                  method = {PUT},
                  consumes = {"a", "b"})
              A surrounded();

              @RequestMapping(method = {PATCH})
              A curly();
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
            import static org.springframework.web.bind.annotation.RequestMethod.POST;
            import static org.springframework.web.bind.annotation.RequestMethod.PUT;

            import org.springframework.web.bind.annotation.DeleteMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PatchMapping;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.PutMapping;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RequestMethod;

            interface A {
              @GetMapping()
              A simple();

              @PostMapping(path = "/foo/bar")
              A prefixed();

              @DeleteMapping(path = "/foo/bar")
              A suffixed();

              @PutMapping(
                  path = "/foo/bar",
                  consumes = {"a", "b"})
              A surrounded();

              @PatchMapping()
              A curly();
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
