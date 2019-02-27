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
}
