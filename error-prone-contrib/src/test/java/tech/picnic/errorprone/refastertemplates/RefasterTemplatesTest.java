package tech.picnic.errorprone.refastertemplates;

import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.refaster.test.RefasterCollectionTestUtil;

final class RefasterTemplatesTest {

  @Test
  void webClientTemplates() {
    RefasterCollectionTestUtil.validateTemplateCollection(WebClientTemplates.class);
  }

  @Test
  void reactorTemplates() {
    RefasterCollectionTestUtil.validateTemplateCollection(ReactorTemplates.class);
  }
}
