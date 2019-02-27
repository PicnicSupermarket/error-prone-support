import java.time.ZoneId;
import java.util.Optional;

final class RefasterCheckPositiveCases {
  boolean testOptionalIsEmpty() {
    return !Optional.empty().isPresent();
  }

  ZoneId testUtcConstant() {
    return ZoneId.of("UTC");
  }
}
