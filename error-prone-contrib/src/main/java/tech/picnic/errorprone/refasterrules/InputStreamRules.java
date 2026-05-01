package tech.picnic.errorprone.refasterrules;

import com.google.common.io.ByteStreams;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link InputStream}s. */
@OnlineDocumentation
final class InputStreamRules {
  private InputStreamRules() {}

  /** Prefer {@link InputStream#transferTo(OutputStream)} over non-JDK alternatives. */
  static final class InputStreamTransferTo {
    @BeforeTemplate
    long before(InputStream from, OutputStream out) throws IOException {
      return ByteStreams.copy(from, out);
    }

    @AfterTemplate
    long after(InputStream from, OutputStream out) throws IOException {
      return from.transferTo(out);
    }
  }

  /** Prefer {@link InputStream#readAllBytes()} over non-JDK alternatives. */
  static final class InputStreamReadAllBytes {
    @BeforeTemplate
    byte[] before(InputStream in) throws IOException {
      return ByteStreams.toByteArray(in);
    }

    @AfterTemplate
    byte[] after(InputStream in) throws IOException {
      return in.readAllBytes();
    }
  }

  /** Prefer {@link InputStream#readNBytes(int)} over non-JDK alternatives. */
  static final class InputStreamReadNBytes {
    @BeforeTemplate
    byte[] before(InputStream in, int len) throws IOException {
      return ByteStreams.limit(in, len).readAllBytes();
    }

    @AfterTemplate
    byte[] after(InputStream in, int len) throws IOException {
      return in.readNBytes(len);
    }
  }

  /** Prefer {@link InputStream#skipNBytes(long)} over non-JDK alternatives. */
  static final class InputStreamSkipNBytes {
    @BeforeTemplate
    void before(InputStream in, long n) throws IOException {
      ByteStreams.skipFully(in, n);
    }

    @AfterTemplate
    void after(InputStream in, long n) throws IOException {
      in.skipNBytes(n);
    }
  }
}
