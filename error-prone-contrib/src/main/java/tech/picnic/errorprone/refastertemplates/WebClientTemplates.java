package tech.picnic.errorprone.refastertemplates;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

/**
 * Refaster templates related to expressions dealing with {@link
 * org.springframework.web.reactive.function.client.WebClient} and related types.
 */
final class WebClientTemplates {
  private WebClientTemplates() {}

  /** Prefer {@link RequestBodySpec#bodyValue(Object)} over more contrived alternatives. */
  static final class BodyValue<T> {
    @BeforeTemplate
    RequestHeadersSpec<?> before(RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.body(fromValue(value));
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before2(
        WebTestClient.RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.body(fromValue(value));
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.bodyValue(value);
    }
  }

  static final class StringIsEmpty {
    @BeforeTemplate
    boolean equalsEmptyString(String string) {
      return string.equals("");
    }

    @AfterTemplate
    boolean optimizedMethod(String string) {
      return string.isEmpty();
    }
  }
}
