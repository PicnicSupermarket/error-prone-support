package tech.picnic.errorprone.refastertemplates;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.util.UriBuilder;

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

  /** Prefer {@link RequestHeadersUriSpec#uri(String, Object...)} over {@link UriBuilder} pattern */
  abstract static class UriBuilderInline {

    @BeforeTemplate
    RequestHeadersSpec<?> before(
        RequestHeadersUriSpec<?> requestHeadersUriSpec, String path, @Repeated Object args) {
      return requestHeadersUriSpec.uri(
          uriBuilder -> uriBuilder.path(path).build(Refaster.asVarargs(args)));
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before2(
        WebTestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec,
        String path,
        @Repeated Object args) {
      return requestHeadersUriSpec.uri(
          uriBuilder -> uriBuilder.path(path).build(Refaster.asVarargs(args)));
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(
        RequestHeadersUriSpec<?> requestHeadersUriSpec, String path, @Repeated Object args) {
      return requestHeadersUriSpec.uri(path, Refaster.asVarargs(args));
    }
  }
}
