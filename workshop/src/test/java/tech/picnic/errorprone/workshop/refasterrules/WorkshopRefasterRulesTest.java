package tech.picnic.errorprone.workshop.refasterrules;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollection;

final class WorkshopRefasterRulesTest {
  @Test
  void validateExampleRuleCollection() {
    RefasterRuleCollection.validate(WorkshopAssignment0Rules.class);
  }

  @Test
  void validateFirstWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment1Rules.class);
  }

  @Test
  void validateSecondWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment2Rules.class);
  }

  @Test
  void validateThirdWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment3Rules.class);
  }

  @Test
  void validateFourthWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment4Rules.class);
  }

  @Test
  void validateFifthWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment5Rules.class);
  }
}
