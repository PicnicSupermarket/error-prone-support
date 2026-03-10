package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NotMatches;
import java.util.Deque;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsList;

/** Refaster rules related to expressions dealing with {@link Deque} instances. */
// XXX: Introduce similar rules for `BlockingDeque`.
@OnlineDocumentation
final class DequeRules {
  private DequeRules() {}

  /** Prefer {@link Deque#addFirst(Object)} over less explicit alternatives. */
  static final class DequeAddFirst<S, T extends S> {
    @BeforeTemplate
    void before(Deque<S> deque, T element) {
      deque.push(element);
    }

    @AfterTemplate
    void after(Deque<S> deque, T element) {
      deque.addFirst(element);
    }
  }

  /**
   * Prefer {@link Deque#addLast(Object)} over less explicit alternatives.
   *
   * <p>Note: this rule does not match instances of type {@link java.util.List} (including {@link
   * java.util.LinkedList}, which also implements {@link Deque}), as {@link
   * java.util.List#add(Object)} is the idiomatic method for those types.
   */
  static final class DequeAddLast<S, T extends S> {
    @BeforeTemplate
    void before(@NotMatches(IsList.class) Deque<S> deque, T element) {
      deque.add(element);
    }

    @AfterTemplate
    void after(Deque<S> deque, T element) {
      deque.addLast(element);
    }
  }

  /** Prefer {@link Deque#removeFirst()} over less explicit alternatives. */
  static final class DequeRemoveFirst<S, T extends S> {
    @BeforeTemplate
    S before(Deque<T> deque) {
      return Refaster.anyOf(deque.pop(), deque.remove());
    }

    @AfterTemplate
    S after(Deque<T> deque) {
      return deque.removeFirst();
    }
  }

  /** Prefer {@link Deque#offerLast(Object)} over less explicit alternatives. */
  static final class DequeOfferLast<S, T extends S> {
    @BeforeTemplate
    boolean before(Deque<S> deque, T element) {
      return deque.offer(element);
    }

    @AfterTemplate
    boolean after(Deque<S> deque, T element) {
      return deque.offerLast(element);
    }
  }

  /** Prefer {@link Deque#pollFirst()} over less explicit alternatives. */
  static final class DequePollFirst<S, T extends S> {
    @BeforeTemplate
    @Nullable S before(Deque<T> deque) {
      return deque.poll();
    }

    @AfterTemplate
    @Nullable S after(Deque<T> deque) {
      return deque.pollFirst();
    }
  }

  /** Prefer {@link Deque#getFirst()} over less explicit alternatives. */
  static final class DequeGetFirst<S, T extends S> {
    @BeforeTemplate
    S before(Deque<T> deque) {
      return deque.element();
    }

    @AfterTemplate
    S after(Deque<T> deque) {
      return deque.getFirst();
    }
  }

  /** Prefer {@link Deque#peekFirst()} over less explicit alternatives. */
  static final class DequePeekFirst<S, T extends S> {
    @BeforeTemplate
    @Nullable S before(Deque<T> deque) {
      return deque.peek();
    }

    @AfterTemplate
    @Nullable S after(Deque<T> deque) {
      return deque.peekFirst();
    }
  }

  /** Prefer {@link Deque#removeFirstOccurrence(Object)} over less explicit alternatives. */
  static final class DequeRemoveFirstOccurrence<T> {
    @BeforeTemplate
    boolean before(Deque<T> deque, Object element) {
      return deque.remove(element);
    }

    @AfterTemplate
    boolean after(Deque<T> deque, Object element) {
      return deque.removeFirstOccurrence(element);
    }
  }

  /** Prefer {@link Deque#iterator()} over more contrived alternatives. */
  static final class DequeIterator<T> {
    @BeforeTemplate
    Iterator<T> before(Deque<T> deque) {
      return deque.reversed().descendingIterator();
    }

    @AfterTemplate
    Iterator<T> after(Deque<T> deque) {
      return deque.iterator();
    }
  }

  /** Prefer {@link Deque#descendingIterator()} over more contrived alternatives. */
  static final class DequeDescendingIterator<T> {
    @BeforeTemplate
    Iterator<T> before(Deque<T> deque) {
      return deque.reversed().iterator();
    }

    @AfterTemplate
    Iterator<T> after(Deque<T> deque) {
      return deque.descendingIterator();
    }
  }
}
