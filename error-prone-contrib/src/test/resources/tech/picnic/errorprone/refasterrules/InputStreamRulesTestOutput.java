package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class IntputStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(ByteStreams.class);
  }

  long testInputStreamTransferTo() {
    return new ByteArrayInputStream().transferTo(new ByteArrayOutputStream());
  }

  byte[] testInputStreamReadAllBytes() {
    return new ByteArrayInputStream().readAllBytes();
  }
}
