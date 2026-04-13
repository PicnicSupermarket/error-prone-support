package tech.picnic.errorprone.refasterrules;

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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/**
 * Refaster rules related to expressions dealing with {@link
 * org.springframework.web.reactive.function.client.WebClient} and related types.
 */
@OnlineDocumentation
final class WebClientRules {
  private WebClientRules() {}

  /** Prefer {@link RequestBodySpec#bodyValue(Object)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class RequestBodySpecBodyValue<T> {
    @BeforeTemplate
    RequestHeadersSpec<?> before(RequestBodySpec spec, T body) {
      return spec.body(fromValue(body));
    }

    @BeforeTemplate
    WebTestClient.RequestHeadersSpec<?> before(WebTestClient.RequestBodySpec spec, T body) {
      return spec.body(fromValue(body));
    }

    @AfterTemplate
    RequestHeadersSpec<?> after(RequestBodySpec spec, T body) {
      return spec.bodyValue(body);
    }
  }

  /** Prefer {@link WebClient#get()} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class WebClientGet {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient client) {
      return client.method(GET);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient client) {
      return client.method(GET);
    }

    @AfterTemplate
    RequestHeadersUriSpec<?> after(WebClient client) {
      return client.get();
    }
  }

  /** Prefer {@link WebClient#head()} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class WebClientHead {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient client) {
      return client.method(HEAD);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient client) {
      return client.method(HEAD);
    }

    @AfterTemplate
    RequestHeadersUriSpec<?> after(WebClient client) {
      return client.head();
    }
  }

  /** Prefer {@link WebClient#options()} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class WebClientOptions {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient client) {
      return client.method(OPTIONS);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient client) {
      return client.method(OPTIONS);
    }

    @AfterTemplate
    RequestHeadersUriSpec<?> after(WebClient client) {
      return client.options();
    }
  }

  /** Prefer {@link WebClient#patch()} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class WebClientPatch {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient client) {
      return client.method(PATCH);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient client) {
      return client.method(PATCH);
    }

    @AfterTemplate
    RequestBodyUriSpec after(WebClient client) {
      return client.patch();
    }
  }

  /** Prefer {@link WebClient#post()} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class WebClientPost {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient client) {
      return client.method(POST);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient client) {
      return client.method(POST);
    }

    @AfterTemplate
    RequestBodyUriSpec after(WebClient client) {
      return client.post();
    }
  }

  /** Prefer {@link WebClient#put()} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class WebClientPut {
    @BeforeTemplate
    RequestBodyUriSpec before(WebClient client) {
      return client.method(PUT);
    }

    @BeforeTemplate
    WebTestClient.RequestBodyUriSpec before(WebTestClient client) {
      return client.method(PUT);
    }

    @AfterTemplate
    RequestBodyUriSpec after(WebClient client) {
      return client.put();
    }
  }

  /**
   * Prefer {@link RequestHeadersUriSpec#uri(String, Object...)} over more contrived alternatives.
   */
  // XXX: Resolve the `RefasterReturnType` warning suppressions by splitting this rule.
  @PossibleSourceIncompatibility
  static final class RequestHeadersUriSpecUri<
      S extends RequestHeadersSpec<S>, T extends WebTestClient.RequestHeadersSpec<T>> {
    @BeforeTemplate
    @SuppressWarnings("RefasterReturnType" /* Generic return type influences matching. */)
    RequestHeadersSpec<?> before(
        RequestHeadersUriSpec<S> spec, String uri, @Repeated Object uriVariables) {
      return spec.uri(uriBuilder -> uriBuilder.path(uri).build(Refaster.asVarargs(uriVariables)));
    }

    @BeforeTemplate
    @SuppressWarnings("RefasterReturnType" /* Generic return type influences matching. */)
    WebTestClient.RequestHeadersSpec<?> before(
        WebTestClient.RequestHeadersUriSpec<T> spec, String uri, @Repeated Object uriVariables) {
      return spec.uri(uriBuilder -> uriBuilder.path(uri).build(Refaster.asVarargs(uriVariables)));
    }

    @AfterTemplate
    S after(RequestHeadersUriSpec<S> spec, String uri, @Repeated Object uriVariables) {
      return spec.uri(uri, Refaster.asVarargs(uriVariables));
    }
  }
}
