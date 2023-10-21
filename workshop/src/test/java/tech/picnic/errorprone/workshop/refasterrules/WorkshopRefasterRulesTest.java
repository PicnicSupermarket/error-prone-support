package tech.picnic.errorprone.workshop.refasterrules;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollection;

final class WorkshopRefasterRulesTest {
  @Disabled("Needs to be implemented in `WorkshopAssignment0Rules.java`.")
  @Test
  void validateExampleRuleCollection() {
    RefasterRuleCollection.validate(WorkshopAssignment0Rules.class);
  }

  @Disabled("Needs to be implemented in `WorkshopAssignment1Rules.java`.")
  @Test
  void validateFirstWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment1Rules.class);
  }

  @Disabled("Needs to be implemented in `WorkshopAssignment2Rules.java`.")
  @Test
  void validateSecondWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment2Rules.class);
  }

  @Disabled("Needs to be implemented in `WorkshopAssignment3Rules.java`.")
  @Test
  void validateThirdWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment3Rules.class);
  }

  @Disabled("Needs to be implemented in `WorkshopAssignment4Rules.java`.")
  @Test
  void validateFourthWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment4Rules.class);
  }

  @Disabled("Needs to be implemented in `WorkshopAssignment5Rules.java`.")
  @Test
  void validateFifthWorkshopAssignment() {
    RefasterRuleCollection.validate(WorkshopAssignment5Rules.class);
  }
}
