package tech.picnic.errorprone.refastertemplates;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import java.util.function.Function;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
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
    WebTestClient.RequestHeadersSpec<?> before(
        WebTestClient.RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.body(fromValue(value));
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.bodyValue(value);
    }
  }

  /**
   * Prefer {@link WebClient#get()} over {@link WebClient#method(HttpMethod)} with {@link
   * HttpMethod#GET}.
   */
  static final class WebClientGet {
    @BeforeTemplate
    RequestHeadersSpec<?> before(WebClient webClient) {
      return webClient.method(GET);
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before(WebTestClient webClient) {
      return webClient.method(GET);
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(WebClient webClient) {
      return webClient.get();
    }
  }

  /**
   * Prefer {@link WebClient#head()} over {@link WebClient#method(HttpMethod)} with {@link
   * HttpMethod#HEAD}.
   */
  static final class WebClientHead {
    @BeforeTemplate
    RequestHeadersSpec<?> before(WebClient webClient) {
      return webClient.method(HEAD);
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before(WebTestClient webClient) {
      return webClient.method(HEAD);
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(WebClient webClient) {
      return webClient.head();
    }
  }

  /**
   * Prefer {@link WebClient#options()} over {@link WebClient#method(HttpMethod)} with {@link
   * HttpMethod#OPTIONS}.
   */
  static final class WebClientOptions {
    @BeforeTemplate
    RequestHeadersSpec<?> before(WebClient webClient) {
      return webClient.method(OPTIONS);
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before(WebTestClient webClient) {
      return webClient.method(OPTIONS);
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(WebClient webClient) {
      return webClient.options();
    }
  }

  /**
   * Prefer {@link WebClient#patch()} over {@link WebClient#method(HttpMethod)} with {@link
   * HttpMethod#PATCH}.
   */
  static final class WebClientPatch {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient webClient) {
      return webClient.method(PATCH);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient webClient) {
      return webClient.method(PATCH);
    }

    @AfterTemplate
    RequestBodyUriSpec after(WebClient webClient) {
      return webClient.patch();
    }
  }

  /**
   * Prefer {@link WebClient#post()} over {@link WebClient#method(HttpMethod)} with {@link
   * HttpMethod#POST}.
   */
  static final class WebClientPost {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient webClient) {
      return webClient.method(POST);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient webClient) {
      return webClient.method(POST);
    }

    @AfterTemplate
    RequestBodyUriSpec after(WebClient webClient) {
      return webClient.post();
    }
  }

  /**
   * Prefer {@link WebClient#put()} over {@link WebClient#method(HttpMethod)} with {@link
   * HttpMethod#PUT}.
   */
  static final class WebClientPut {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient webClient) {
      return webClient.method(PUT);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient webClient) {
      return webClient.method(PUT);
    }

    @AfterTemplate
    RequestBodyUriSpec after(WebClient webClient) {
      return webClient.put();
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
