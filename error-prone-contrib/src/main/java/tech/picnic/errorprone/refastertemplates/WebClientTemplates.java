package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

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

  abstract static class UriBuilderInline {

    @Placeholder
    abstract UriBuilder uriBuilderPlaceholder ();

    @BeforeTemplate
    RequestHeadersSpec<?> before (WebClient.RequestHeadersUriSpec<?> requestBodyUriSpec, String path, Object... values) {
      return requestBodyUriSpec
              .uri(uriBuilder -> uriBuilderPlaceholder()
                      .path(path)
                      .build(values)
              );
    }

    @AfterTemplate
    RequestHeadersSpec<?> after (WebClient.RequestHeadersUriSpec<?> requestBodyUriSpec, String path, Object... values) {
      return requestBodyUriSpec.uri(path, values);
    }
  }



}
