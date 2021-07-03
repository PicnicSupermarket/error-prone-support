package tech.picnic.errorprone.refastertemplates;

import static java.util.function.Function.identity;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  static final class RetrieveArray<T> {
    @BeforeTemplate
    Flux<T> before(WebClient.ResponseSpec responseSpec) {
      return responseSpec.bodyToMono(Refaster.<T[]>clazz()).flux().flatMap(Flux::fromArray);
    }

    @AfterTemplate
    Flux<T> after(WebClient.ResponseSpec responseSpec) {
      return responseSpec.bodyToFlux(Refaster.<T>clazz());
    }
  }

  static final class RetrieveParameterizedTypeReference<T> {
    @BeforeTemplate
    Flux<T> before(
        WebClient.ResponseSpec responseSpec,
        ParameterizedTypeReference<? extends Collection<T>> clazz) {
      return responseSpec.bodyToMono(clazz).flux().flatMapIterable(identity());
    }

    @AfterTemplate
    Flux<T> after(WebClient.ResponseSpec responseSpec) {
      return responseSpec.bodyToFlux(Refaster.<T>clazz());
    }
  }

  static final class RetrieveSingle<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Class<T> clazz) {
      return mono.flux().single();
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Class<T> clazz) {
      return mono.single();
    }
  }

  static final class RetrieveSingleOrEmpty<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Class<T> clazz) {
      return mono.flux().singleOrEmpty();
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }
}
