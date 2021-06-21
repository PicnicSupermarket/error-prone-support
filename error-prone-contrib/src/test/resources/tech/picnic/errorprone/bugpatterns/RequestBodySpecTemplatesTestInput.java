package tech.picnic.errorprone.bugpatterns;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

final class RequestBodySpecTemplatesTest implements RefasterTemplateTestCase {
  public void testBodyValue() {
    WebClient.create("foo").post().body(fromValue("bar"));

    WebTestClient.bindToServer().build().post().body(fromValue("bar"));

    // Extra usage of `fromValue` to prevent the test from failing. The import will be unused and
    // cause the build to fail.
    fromValue("");
  }
}
