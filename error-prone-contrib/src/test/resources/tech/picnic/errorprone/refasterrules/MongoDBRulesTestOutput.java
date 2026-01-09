package tech.picnic.errorprone.refasterrules;

import static com.mongodb.client.model.Filters.eq;

import org.bson.conversions.Bson;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;
import tech.picnic.errorprone.utils.ThirdPartyLibrary;

final class MongoDBRulesTest implements RefasterRuleCollectionTestCase {
  Bson testEq() {
    return eq("foo", ThirdPartyLibrary.GUAVA);
  }
}
