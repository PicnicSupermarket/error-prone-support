package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.assertj.core.api.AbstractMapAssert;

final class AssertJMapTemplatesTest implements RefasterTemplateTestCase {
  AbstractMapAssert<?, ?, Integer, Integer> testAbstractMapAssertContainsExactlyEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2, 3, 4))
        .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(1, 2, 3, 4));
  }

  AbstractMapAssert<?, ?, Integer, Integer> testAbstractMapAssertContainsOnly() {
    return assertThat(ImmutableMap.of(1, 2)).containsOnly(Map.entry(1, 2));
  }
}
