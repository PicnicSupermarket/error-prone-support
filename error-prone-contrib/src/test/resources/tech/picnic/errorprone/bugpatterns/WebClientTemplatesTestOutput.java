package tech.picnic.errorprone.bugpatterns;

import static java.util.function.Function.identity;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class WebClientTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        fromValue(""),
        identity(),
        Flux.class,
        ImmutableList.class,
        ParameterizedTypeReference.class);
  }

  ImmutableSet<?> testBodyValue() {
    return ImmutableSet.of(
        WebClient.create("foo").post().bodyValue("bar"),
        WebTestClient.bindToServer().build().post().bodyValue("bar"));
  }

  public void testBodyToFluxValue() {
    WebClient.create("foo").get().retrieve().bodyToFlux(Integer.class);
  }

  public void testOther() {
    WebClient.create("foo").get().retrieve().bodyToFlux(String.class);
  }

  public void testCase3() {
    Mono.empty().single();
  }

  public void testCase4() {
    Mono.empty();
  }
}
