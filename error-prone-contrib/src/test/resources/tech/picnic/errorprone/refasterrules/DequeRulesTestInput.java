package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Iterator;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class DequeRulesTest implements RefasterRuleCollectionTestCase {
  void testDequeAddFirst() {
    new ArrayDeque<String>().push("foo");
  }

  void testDequeAddLast() {
    new ArrayDeque<String>().add("foo");
  }

  ImmutableSet<String> testDequeRemoveFirst() {
    return ImmutableSet.of(new ArrayDeque<String>(0).pop(), new ArrayDeque<String>(1).remove());
  }

  boolean testDequeOfferLast() {
    return new ArrayDeque<String>().offer("foo");
  }

  String testDequePollFirst() {
    return new ArrayDeque<String>().poll();
  }

  String testDequeGetFirst() {
    return new ArrayDeque<String>().element();
  }

  String testDequePeekFirst() {
    return new ArrayDeque<String>().peek();
  }

  boolean testDequeRemoveFirstOccurrence() {
    return new ArrayDeque<>().remove("foo");
  }

  Iterator<String> testDequeIterator() {
    return new ArrayDeque<String>().reversed().descendingIterator();
  }

  Iterator<String> testDequeDescendingIterator() {
    return new ArrayDeque<String>().reversed().iterator();
  }
}
