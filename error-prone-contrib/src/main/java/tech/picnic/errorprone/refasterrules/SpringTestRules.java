package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to Spring Test expressions and statements. */
@OnlineDocumentation
final class SpringTestRules {
  private SpringTestRules() {}

  /**
   * Prefer {@link BodyContentSpec#json(String, JsonCompareMode)} over alternatives that implicitly
   * perform a {@link JsonCompareMode#LENIENT lenient} comparison or are deprecated.
   */
  static final class BodyContentSpecJsonLenient {
    @BeforeTemplate
    @SuppressWarnings("deprecation" /* This deprecated method invocation will be rewritten. */)
    BodyContentSpec before(BodyContentSpec spec, String expectedJson) {
      return Refaster.anyOf(spec.json(expectedJson), spec.json(expectedJson, /* strict= */ false));
    }

    @AfterTemplate
    BodyContentSpec after(BodyContentSpec spec, String expectedJson) {
      return spec.json(expectedJson, JsonCompareMode.LENIENT);
    }
  }

  /**
   * Prefer {@link BodyContentSpec#json(String, JsonCompareMode)} over the deprecated alternative.
   */
  static final class BodyContentSpecJsonStrict {
    @BeforeTemplate
    @SuppressWarnings("deprecation" /* This deprecated method invocation will be rewritten. */)
    BodyContentSpec before(BodyContentSpec spec, String expectedJson) {
      return spec.json(expectedJson, /* strict= */ true);
    }

    @AfterTemplate
    BodyContentSpec after(BodyContentSpec spec, String expectedJson) {
      return spec.json(expectedJson, JsonCompareMode.STRICT);
    }
  }
}
