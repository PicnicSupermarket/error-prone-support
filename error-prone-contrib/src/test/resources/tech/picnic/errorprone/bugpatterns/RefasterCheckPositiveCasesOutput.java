import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

final class RefasterCheckPositiveCases {
  boolean testOptionalIsEmpty() {
    return Optional.empty().isEmpty();
  }

  ZoneId testUtcConstant() {
    return ZoneOffset.UTC;
  }
}
