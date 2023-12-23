package tech.picnic.errorprone.refasterrules.output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class FileRulesTest implements RefasterRuleCollectionTestCase {
  String testFilesReadStringWithCharset() throws IOException {
    return Files.readString(Paths.get("foo"), StandardCharsets.ISO_8859_1);
  }

  String testFilesReadString() throws IOException {
    return Files.readString(Paths.get("foo"));
  }
}
