package tech.picnic.errorprone.refasterrules;

import java.util.stream.Stream;
import org.jooq.impl.DSL;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JooqRulesTest implements RefasterRuleCollectionTestCase {
  Stream<?> testResultQueryFetchStream() {
    return DSL.select().fetch().stream();
  }
}
