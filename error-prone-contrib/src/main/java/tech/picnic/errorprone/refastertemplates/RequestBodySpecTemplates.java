package tech.picnic.errorprone.refastertemplates;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

/** Refaster template to improve readability of {@link RequestBodySpec#body(Publisher, Class)} */
final class RequestBodySpecTemplates {
  private RequestBodySpecTemplates() {}

  /** Prefer using {@link RequestBodySpec#bodyValue(Object)} */
  static final class BodyValue<T> {
    @BeforeTemplate
    public RequestHeadersSpec<?> before(RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.body(fromValue(value));
    }

    @AfterTemplate
    public RequestHeadersSpec<?> after(RequestBodySpec requestBodySpec, T value) {
      return requestBodySpec.bodyValue(value);
    }
  }
}
