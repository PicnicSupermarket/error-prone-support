package tech.picnic.errorprone.refastertemplates;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import java.util.function.Function;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;

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

  /** Don't unnecessarily use {@link RequestHeadersUriSpec#uri(Function)}. */
  abstract static class RequestHeadersUriSpecUri {
    @BeforeTemplate
    RequestHeadersSpec<?> before(
        RequestHeadersUriSpec<?> requestHeadersUriSpec,
        String path,
        @Repeated Object uriVariables) {
      return requestHeadersUriSpec.uri(
          uriBuilder -> uriBuilder.path(path).build(Refaster.asVarargs(uriVariables)));
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before(
        WebTestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec,
        String path,
        @Repeated Object uriVariables) {
      return requestHeadersUriSpec.uri(
          uriBuilder -> uriBuilder.path(path).build(Refaster.asVarargs(uriVariables)));
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(
        RequestHeadersUriSpec<?> requestHeadersUriSpec,
        String path,
        @Repeated Object uriVariables) {
      return requestHeadersUriSpec.uri(path, Refaster.asVarargs(uriVariables));
    }
  }
}
