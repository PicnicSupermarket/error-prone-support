package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Flowable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaUnwrapTemplatesTest implements RefasterTemplateTestCase {
  Flowable<Integer> testFlowableBiFunctionRemoveUtil() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(Flowable.just(1), Flowable.just(2), (i1, i2) -> i1 + i2));
  }
}
