package tech.picnic.errorprone.bugpatterns;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import org.springframework.web.reactive.function.client.WebClient;

final class RequestBodySpecTemplatesTest implements RefasterTemplateTestCase {
  public void testBodyValue() {
    WebClient.create("foo").post().body(fromValue("bar")).retrieve();
  }
}
