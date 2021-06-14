package tech.picnic.errorprone.bugpatterns;

import org.springframework.web.reactive.function.client.WebClient;

final class RequestBodySpecTemplatesTest implements RefasterTemplateTestCase {
  public void testBodyValue() {
    WebClient.create("foo").post().bodyValue("bar").retrieve();
  }
}
