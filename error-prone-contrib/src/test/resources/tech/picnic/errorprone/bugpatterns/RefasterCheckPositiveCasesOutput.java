import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

final class RefasterCheckPositiveCases {
  boolean testEqualBooleans(boolean b1, boolean b2) {
    return b1 == b2;
  }

  boolean testEqualBooleansNegation(boolean b1, boolean b2) {
    return b1 != b2;
  }

  boolean testOptionalIsEmpty() {
    return Optional.empty().isEmpty();
  }

  ZoneId testUtcConstant() {
    return ZoneOffset.UTC;
  }
}
