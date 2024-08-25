package tech.picnic.errorprone.documentation;

import java.io.IOException;
import org.junit.jupiter.api.Test;

final class JekyllCollectionGeneratorTest {

  @Test
  void foo() throws IOException {
    JekyllCollectionGenerator.main(
        new String[] {"/home/sschroevers/workspace/picnic/error-prone-support"});
  }
}
