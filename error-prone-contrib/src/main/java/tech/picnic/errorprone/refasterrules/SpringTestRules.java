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
   * Prefer {@link BodyContentSpec#json(String, JsonCompareMode)} with lenient mode over deprecated
   * alternatives.
   */
  static final class BodyContentSpecJsonJsonCompareModeLenient {
    @BeforeTemplate
    @SuppressWarnings("deprecation" /* This deprecated API usage will be rewritten. */)
    BodyContentSpec before(BodyContentSpec bodyContentSpec, String expectedJson) {
      return Refaster.anyOf(
          bodyContentSpec.json(expectedJson),
          bodyContentSpec.json(expectedJson, /* strict= */ false));
    }

    @AfterTemplate
    BodyContentSpec after(BodyContentSpec bodyContentSpec, String expectedJson) {
      return bodyContentSpec.json(expectedJson, JsonCompareMode.LENIENT);
    }
  }

  /**
   * Prefer {@link BodyContentSpec#json(String, JsonCompareMode)} with strict mode over deprecated
   * alternatives.
   */
  static final class BodyContentSpecJsonJsonCompareModeStrict {
    @BeforeTemplate
    @SuppressWarnings("deprecation" /* This deprecated API usage will be rewritten. */)
    BodyContentSpec before(BodyContentSpec bodyContentSpec, String expectedJson) {
      return bodyContentSpec.json(expectedJson, /* strict= */ true);
    }

    @AfterTemplate
    BodyContentSpec after(BodyContentSpec bodyContentSpec, String expectedJson) {
      return bodyContentSpec.json(expectedJson, JsonCompareMode.STRICT);
    }
  }
}
