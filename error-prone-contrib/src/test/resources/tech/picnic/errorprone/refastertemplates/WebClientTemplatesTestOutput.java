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

  ImmutableSet<?> testUriBuilder() {
    return ImmutableSet.of(
        WebClient.create("foo").get().uri("/bar/{baz}/uri", "BAZ"),
        WebTestClient.bindToServer().build().get().uri("/bar/{baz}/uri", "BAZ"),
        WebClient.create("bar").post().uri("/foo/{baz}/{bar}/{foo}/bar", "BAZ", "BAR", "FOOD"),
        WebTestClient.bindToServer()
            .build()
            .post()
            .uri("/foo/{baz}/{bar}/{foo}/bar", "BAZ", "BAR", "FOOD"));
  }
}
