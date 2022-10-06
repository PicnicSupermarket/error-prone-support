package tech.picnic.errorprone.refastertemplates.input;

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
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class WebClientTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(fromValue(""), GET, HEAD, OPTIONS, PATCH, POST, PUT);
  }

  ImmutableSet<?> testBodyValue() {
    return ImmutableSet.of(
        WebClient.create().post().body(fromValue("bar")),
        WebTestClient.bindToServer().build().post().body(fromValue("bar")));
  }

  ImmutableSet<?> testWebClientGet() {
    return ImmutableSet.of(
        WebClient.create().method(GET), WebTestClient.bindToServer().build().method(GET));
  }

  ImmutableSet<?> testWebClientHead() {
    return ImmutableSet.of(
        WebClient.create().method(HEAD), WebTestClient.bindToServer().build().method(HEAD));
  }

  ImmutableSet<?> testWebClientOptions() {
    return ImmutableSet.of(
        WebClient.create().method(OPTIONS), WebTestClient.bindToServer().build().method(OPTIONS));
  }

  ImmutableSet<?> testWebClientPatch() {
    return ImmutableSet.of(
        WebClient.create().method(PATCH), WebTestClient.bindToServer().build().method(PATCH));
  }

  ImmutableSet<?> testWebClientPost() {
    return ImmutableSet.of(
        WebClient.create().method(POST), WebTestClient.bindToServer().build().method(POST));
  }

  ImmutableSet<?> testWebClientPut() {
    return ImmutableSet.of(
        WebClient.create().method(PUT), WebTestClient.bindToServer().build().method(PUT));
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
