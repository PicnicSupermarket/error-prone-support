package tech.picnic.errorprone.refasterrules;

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
        WebClient.create().post().bodyValue("bar"),
        WebTestClient.bindToServer().build().post().bodyValue("bar"));
  }

  ImmutableSet<?> testWebClientGet() {
    return ImmutableSet.of(WebClient.create().get(), WebTestClient.bindToServer().build().get());
  }

  ImmutableSet<?> testWebClientHead() {
    return ImmutableSet.of(WebClient.create().head(), WebTestClient.bindToServer().build().head());
  }

  ImmutableSet<?> testWebClientOptions() {
    return ImmutableSet.of(
        WebClient.create().options(), WebTestClient.bindToServer().build().options());
  }

  ImmutableSet<?> testWebClientPatch() {
    return ImmutableSet.of(
        WebClient.create().patch(), WebTestClient.bindToServer().build().patch());
  }

  ImmutableSet<?> testWebClientPost() {
    return ImmutableSet.of(WebClient.create().post(), WebTestClient.bindToServer().build().post());
  }

  ImmutableSet<?> testWebClientPut() {
    return ImmutableSet.of(WebClient.create().put(), WebTestClient.bindToServer().build().put());
  }

  ImmutableSet<?> testRequestHeadersUriSpecUri() {
    return ImmutableSet.of(
        WebClient.create("foo").get().uri("/bar"),
        WebClient.create("bar").post().uri("/bar/{baz}", "quux"),
        WebTestClient.bindToServer().build().get().uri("/baz"),
        WebTestClient.bindToServer().build().post().uri("/qux/{quux}/{quuz}", "corge", "grault"));
  }
}
