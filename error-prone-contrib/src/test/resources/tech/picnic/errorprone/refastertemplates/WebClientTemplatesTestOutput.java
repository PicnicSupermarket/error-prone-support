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
        WebClient.create("foo").post().bodyValue("bar"),
        WebTestClient.bindToServer().build().post().bodyValue("bar"));
  }

  ImmutableSet<?> testGetMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").get(), WebTestClient.bindToServer().build().get());
  }

  ImmutableSet<?> testHeadMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").head(), WebTestClient.bindToServer().build().head());
  }

  ImmutableSet<?> testOptionsMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").options(), WebTestClient.bindToServer().build().options());
  }

  ImmutableSet<?> testPatchMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").patch(), WebTestClient.bindToServer().build().patch());
  }

  ImmutableSet<?> testPostMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").post(), WebTestClient.bindToServer().build().post());
  }

  ImmutableSet<?> testPutMethod() {
    return ImmutableSet.of(
        WebClient.create("foo").put(), WebTestClient.bindToServer().build().put());
  }

  ImmutableSet<?> testRequestHeadersUriSpecUri() {
    return ImmutableSet.of(
        WebClient.create("foo").get().uri("/bar"),
        WebClient.create("bar").post().uri("/bar/{baz}", "quux"),
        WebTestClient.bindToServer().build().get().uri("/baz"),
        WebTestClient.bindToServer().build().post().uri("/qux/{quux}/{quuz}", "corge", "grault"));
  }
}
