package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;

final class PrimitiveTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Ints.class);
  }

  ImmutableSet<Boolean> testLessThan() {
    return ImmutableSet.of(
        !((byte) 3 >= (byte) 4),
        !((short) 3 >= (short) 4),
        !(3 >= 4),
        !(3L >= 4L),
        !(3F >= 4F),
        !(3.0 >= 4.0));
  }

  ImmutableSet<Boolean> testLessThanOrEqualTo() {
    return ImmutableSet.of(
        !((byte) 3 > (byte) 4),
        !((short) 3 > (short) 4),
        !(3 > 4),
        !(3L > 4L),
        !(3F > 4F),
        !(3.0 > 4.0));
  }

  ImmutableSet<Boolean> testGreaterThan() {
    return ImmutableSet.of(
        !((byte) 3 <= (byte) 4),
        !((short) 3 <= (short) 4),
        !(3 <= 4),
        !(3L <= 4L),
        !(3F <= 4F),
        !(3.0 <= 4.0));
  }

  ImmutableSet<Boolean> testGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        !((byte) 3 < (byte) 4),
        !((short) 3 < (short) 4),
        !(3 < 4),
        !(3L < 4L),
        !(3F < 4F),
        !(3.0 < 4.0));
  }

  int testLongToIntExact() {
    return Ints.checkedCast(Long.MAX_VALUE);
  }
}
