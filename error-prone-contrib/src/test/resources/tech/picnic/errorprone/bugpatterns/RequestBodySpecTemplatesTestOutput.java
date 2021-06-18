package tech.picnic.errorprone.bugpatterns;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import org.springframework.web.reactive.function.client.WebClient;

final class RequestBodySpecTemplatesTest implements RefasterTemplateTestCase {
  public void testBodyValue() {
    WebClient.create("foo").post().bodyValue("bar").retrieve();

    // Extra usage of `fromValue` to prevent the test from failing. The import will be unused and
    // cause the build to fail.
    fromValue("");
  }
}
