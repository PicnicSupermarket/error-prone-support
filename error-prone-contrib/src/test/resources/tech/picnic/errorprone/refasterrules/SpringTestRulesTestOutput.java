package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SpringTestRulesTest implements RefasterRuleCollectionTestCase {
  @SuppressWarnings("deprecation" /* Rule rewrites deprecated method invocation. */)
  ImmutableSet<BodyContentSpec> testBodyContentSpecJsonLenient() {
    return ImmutableSet.of(
        ((BodyContentSpec) null).json("foo", JsonCompareMode.LENIENT),
        ((BodyContentSpec) null).json("bar", JsonCompareMode.LENIENT));
  }

  @SuppressWarnings("deprecation" /* Rule rewrites deprecated method invocation. */)
  BodyContentSpec testBodyContentSpecJsonStrict() {
    return ((BodyContentSpec) null).json("foo", JsonCompareMode.STRICT);
  }
}
