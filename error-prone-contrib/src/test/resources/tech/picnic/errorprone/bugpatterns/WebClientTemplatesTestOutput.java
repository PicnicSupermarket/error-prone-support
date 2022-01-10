package tech.picnic.errorprone.bugpatterns;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.common.collect.ImmutableSet;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class WebClientTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(fromValue(""));
  }

  ImmutableSet<?> testBodyVallue() {
    return ImmutableSet.of(
        WebClient.create("foo").post().bodyValue("bar"),
        WebClient.create("foo").post().bodyValue("bar"),
        WebTestClient.bindToServer().build().post().bodyValue("bar"));
  }
}
