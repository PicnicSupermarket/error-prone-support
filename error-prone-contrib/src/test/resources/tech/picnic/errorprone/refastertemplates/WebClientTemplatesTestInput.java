package tech.picnic.errorprone.refastertemplates;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.common.collect.ImmutableSet;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

final class WebClientTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(fromValue(""), GET, POST, PUT, PATCH, HEAD, OPTIONS);
  }

  ImmutableSet<?> testBodyValue() {
    return ImmutableSet.of(
        WebClient.create("foo").post().body(fromValue("bar")),
        WebTestClient.bindToServer().build().post().body(fromValue("bar")));
  }

  ImmutableSet<?> testGetMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").method(GET), WebTestClient.bindToServer().build().method(GET));
  }

  ImmutableSet<?> testHeadMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").method(HEAD), WebTestClient.bindToServer().build().method(HEAD));
  }

  ImmutableSet<?> testOptionsMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").method(OPTIONS),
        WebTestClient.bindToServer().build().method(OPTIONS));
  }

  ImmutableSet<?> testPatchMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").method(PATCH), WebTestClient.bindToServer().build().method(PATCH));
  }

  ImmutableSet<?> testPostMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").method(POST), WebTestClient.bindToServer().build().method(POST));
  }

  ImmutableSet<?> testPutMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").method(PUT), WebTestClient.bindToServer().build().method(PUT));
  }

  ImmutableSet<?> testRequestHeadersUriSpecUri() {
    return ImmutableSet.of(
        WebClient.create("foo").get().uri(uriBuilder -> uriBuilder.path("/bar").build()),
        WebClient.create("bar")
            .post()
            .uri(uriBuilder -> uriBuilder.path("/bar/{baz}").build("quux")),
        WebTestClient.bindToServer()
            .build()
            .get()
            .uri(uriBuilder -> uriBuilder.path("/baz").build()),
        WebTestClient.bindToServer()
            .build()
            .post()
            .uri(uriBuilder -> uriBuilder.path("/qux/{quux}/{quuz}").build("corge", "grault")));
  }
}
