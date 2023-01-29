package tech.picnic.errorprone.bugpatterns.util;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

/**
 * Class that has members like {@link Flux} but is not related to it.
 *
 * <p>This class meant to help verify that matchers only pick up {@link Flux} members instead of
 * everything that has the same signature.
 *
 * @param <T> has no purpose other than to match the signature of {@link Flux}.
 */
public final class NotFlux<T> {
  /**
   * mocks {@link Flux#toIterable()}.
   *
   * @return empty {@link Iterable}.
   */
  @SuppressWarnings("PreferredInterfaceType")
  public Iterable<T> toIterable() {
    return ImmutableList.of();
  }

  /**
   * mocks {@link Flux#toStream()}.
   *
   * @return empty {@link Stream}.
   */
  public Stream<T> toStream() {
    return Stream.empty();
  }
}
