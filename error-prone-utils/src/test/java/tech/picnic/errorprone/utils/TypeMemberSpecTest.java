package tech.picnic.errorprone.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

final class TypeMemberSpecTest {
  @Test
  void parseWholeType() {
    assertThat(TypeMemberSpec.parse(Math.class.getCanonicalName()))
        .isEqualTo(new TypeMemberSpec(Math.class.getCanonicalName(), Optional.empty()));
  }

  @Test
  void parseTypeAndMember() {
    assertThat(TypeMemberSpec.parse("java.lang.Math#max"))
        .isEqualTo(new TypeMemberSpec(Math.class.getCanonicalName(), Optional.of("max")));
  }

  @Test
  void parseSplitsOnFirstHashOnly() {
    assertThat(TypeMemberSpec.parse("java.lang.Math#max#min"))
        .isEqualTo(new TypeMemberSpec(Math.class.getCanonicalName(), Optional.of("max#min")));
  }
}
