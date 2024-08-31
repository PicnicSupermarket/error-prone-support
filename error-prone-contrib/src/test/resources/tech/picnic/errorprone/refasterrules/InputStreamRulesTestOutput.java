package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class InputStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(ByteStreams.class);
  }

  long testInputStreamTransferTo() throws IOException {
    return new ByteArrayInputStream(new byte[0]).transferTo(new ByteArrayOutputStream());
  }

  byte[] testInputStreamReadAllBytes() throws IOException {
    return new ByteArrayInputStream(new byte[0]).readAllBytes();
  }

  byte[] testInputStreamReadNBytes() throws IOException {
    return new ByteArrayInputStream(new byte[0]).readNBytes(0);
  }

  void testInputStreamSkipNBytes() throws IOException {
    new ByteArrayInputStream(new byte[0]).skipNBytes(0);
  }
}
