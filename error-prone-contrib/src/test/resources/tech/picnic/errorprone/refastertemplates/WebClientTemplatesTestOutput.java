package tech.picnic.errorprone.refastertemplates;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.common.collect.ImmutableSet;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

final class WebClientTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(fromValue(""));
  }

  ImmutableSet<?> testBodyValue() {
    return ImmutableSet.of(
        WebClient.create("foo").post().bodyValue("bar"),
        WebTestClient.bindToServer().build().post().bodyValue("bar"));
  }

  ImmutableSet<?> testRequestHeadersUriSpecUri() {
    return ImmutableSet.of(
        WebClient.create("foo").get().uri("/bar/{baz}", "BAZ"),
        WebTestClient.bindToServer().build().get().uri("/bar/{baz}", "BAZ"),
        WebClient.create("bar").post().uri("/foo/{bar}/{baz}/{qux}/quux", "BAR", "BAZ", "QUX"),
        WebTestClient.bindToServer()
            .build()
            .post()
            .uri("/foo/{bar}/{baz}/{qux}/quux", "BAR", "BAZ", "QUX"));
  }
}
