package tech.picnic.errorprone.refasterrules.input;

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
    return ByteStreams.copy(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream());
  }

  byte[] testInputStreamReadAllBytes() throws IOException {
    return ByteStreams.toByteArray(new ByteArrayInputStream(new byte[0]));
  }

  byte[] testInputStreamReadNBytes() throws IOException {
    return ByteStreams.limit(new ByteArrayInputStream(new byte[0]), 0).readAllBytes();
  }

  void testInputStreamSkipNBytes() throws IOException {
    ByteStreams.skipFully(new ByteArrayInputStream(new byte[0]), 0);
  }
}
