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
        WebClient.create("foo").post().body(fromValue("bar")),
        WebTestClient.bindToServer().build().post().body(fromValue("bar")));
  }

  ImmutableSet<?> testRequestHeadersUriSpecUri() {
    return ImmutableSet.of(
        WebClient.create("foo").get().uri(uriBuilder -> uriBuilder.path("/bar/{baz}").build("BAZ")),
        WebTestClient.bindToServer()
            .build()
            .get()
            .uri(uriBuilder -> uriBuilder.path("/bar/{baz}").build("BAZ")),
        WebClient.create("bar")
            .post()
            .uri(
                uriBuilder ->
                    uriBuilder.path("/foo/{bar}/{baz}/{qux}/quux").build("BAR", "BAZ", "QUX")),
        WebTestClient.bindToServer()
            .build()
            .post()
            .uri(
                uriBuilder ->
                    uriBuilder.path("/foo/{bar}/{baz}/{qux}/quux").build("BAR", "BAZ", "QUX")));
  }
}
