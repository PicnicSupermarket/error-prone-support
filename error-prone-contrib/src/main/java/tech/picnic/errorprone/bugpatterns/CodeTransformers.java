package tech.picnic.errorprone.bugpatterns;

import com.google.common.hash.Hashing;
import com.google.common.reflect.ClassPath;
import java.io.IOException;

final class CodeTransformers {
  // XXX: Use.
  private static void foo(ClassPath.ResourceInfo resource) {
    try {
      resource.asByteSource().hash(Hashing.murmur3_32_fixed(0)).toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
