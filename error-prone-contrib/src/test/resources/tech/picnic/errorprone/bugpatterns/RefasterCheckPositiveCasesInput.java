import java.time.ZoneId;
import java.util.Optional;

final class RefasterCheckPositiveCases {
  boolean testEqualBooleans(boolean b1, boolean b2) {
    return b1 ? b2 : !b2;
  }

  boolean testEqualBooleansNegation(boolean b1, boolean b2) {
    return b1 ? !b2 : b2;
  }

  boolean testOptionalIsEmpty() {
    return !Optional.empty().isPresent();
  }

  ZoneId testUtcConstant() {
    return ZoneId.of("UTC");
  }

  // XXX: Doesn't work. Why is `.asList()` not dropped?
  //  void testImmutableCollectionAsListToStream() {
  //    ImmutableSet.of(1, 2).asList().stream().collect(toImmutableMap(identity(), identity()));
  //  }
  //
  //  <T> void testImmutableCollectionAsListToStream(ImmutableCollection<T> collection) {
  //    collection.asList().stream();
  //  }
}
