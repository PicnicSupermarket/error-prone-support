package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class DequeRulesTest implements RefasterRuleCollectionTestCase {
  void testDequeAddFirst() {
    new ArrayDeque<String>().addFirst("foo");
  }

  void testDequeAddLast() {
    new LinkedList<String>().add("foo");
    new ArrayDeque<String>().addLast("bar");
  }

  ImmutableSet<String> testDequeRemoveFirst() {
    return ImmutableSet.of(
        new ArrayDeque<String>(1).removeFirst(), new ArrayDeque<String>(2).removeFirst());
  }

  boolean testDequeOfferLast() {
    return new ArrayDeque<String>().offerLast("foo");
  }

  String testDequePollFirst() {
    return new ArrayDeque<String>().pollFirst();
  }

  String testDequeGetFirst() {
    return new ArrayDeque<String>().getFirst();
  }

  String testDequePeekFirst() {
    return new ArrayDeque<String>().peekFirst();
  }

  boolean testDequeRemoveFirstOccurrence() {
    return new ArrayDeque<>().removeFirstOccurrence("foo");
  }

  Iterator<String> testDequeIterator() {
    return new ArrayDeque<String>().iterator();
  }

  Iterator<String> testDequeDescendingIterator() {
    return new ArrayDeque<String>().descendingIterator();
  }
}
