package tech.picnic.errorprone.refastertemplates;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.google.common.collect.ImmutableSet;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

final class WebClientTemplatesTest implements RefasterTemplateTestCase {
    @Override
    public ImmutableSet<?> elidedTypesAndStaticImports() {
        return ImmutableSet.of(fromValue(""));
    }

    ImmutableSet<?> testBodyValue() {
        return ImmutableSet.of(
                WebClient.create("foo").post().body(fromValue("bar")),
                WebTestClient.bindToServer().build().post().body(fromValue("bar")));
    }


    ImmutableSet<?> testUriBuilder() {
        return ImmutableSet.of(
                WebClient.create("foo").post().uri(uriBuilder ->
                        uriBuilder.path("/bar/{baz}/uri").build("BAZ")));
    }
}
