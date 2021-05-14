package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;

import io.reactivex.Flowable;
import reactor.adapter.rxjava.RxJava2Adapter;

final class RxJavaToReactorTemplatesTest implements RefasterTemplateTestCase {
  Flowable<Object> testFlowableFlatMapInReactor() { // look at the return type...
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(i -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);
  }
}
