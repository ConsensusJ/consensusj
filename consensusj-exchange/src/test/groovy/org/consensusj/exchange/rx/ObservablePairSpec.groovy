package org.consensusj.exchange.rx

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.consensusj.exchange.PollableIdentityExchangeProvider
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 *
 */
class ObservablePairSpec extends Specification {
    def "Quick test with just"() {
        given:
        Observable<Long> fakeInterval = Observable.just(0L,0L,0L)
        PollableIdentityExchangeProvider provider = new PollableIdentityExchangeProvider()
        ObservablePair observablePair = new ObservablePair(provider,  provider.identityPair, fakeInterval)
        List<ExchangeRateUpdate> output = new ArrayList<>()

        when:
        Disposable disposable = observablePair.subscribe({ update -> output.add(update) })

        then:
        output.size() == 3
        output.get(0).pair == provider.identityPair
        output.get(0).currentFactor == provider.fixedRate
        output.get(0).serverTimeStamp == 0
        disposable.isDisposed()
    }

    def "Test with interval and TestScheduler"() {
        given:
        TestScheduler scheduler = new TestScheduler();
        Observable<Long> fakeInterval = Observable.interval(1, TimeUnit.SECONDS, scheduler);
        PollableIdentityExchangeProvider provider = new PollableIdentityExchangeProvider()
        ObservablePair observablePair = new ObservablePair(provider,  provider.identityPair, fakeInterval)
        List<ExchangeRateUpdate> output1 = new ArrayList<>()
        List<ExchangeRateUpdate> output2 = new ArrayList<>()
        List<ExchangeRateUpdate> output3 = new ArrayList<>()

        when:
        Disposable subscriber1 = observablePair.subscribe( {output1.add(it)}, {}, {})
        Disposable subscriber2 = observablePair.subscribe( {output2.add(it)}, {}, {})

        then:
        output1.size() == 0
        output2.size() == 0
        observablePair.pollCount == 0
        !subscriber1.isDisposed()
        !subscriber2.isDisposed()

        when:
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        then:
        output1.size() == 1
        output2.size() == 1
        observablePair.pollCount == 1
        !subscriber1.isDisposed()
        !subscriber2.isDisposed()

        when:
        subscriber1.dispose()
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        then:
        output1.size() == 1
        output2.size() == 2
        observablePair.pollCount == 2
        subscriber1.isDisposed()
        !subscriber2.isDisposed()

        when:
        subscriber2.dispose()
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        observablePair.pollCount == 2

        when:
        Disposable subscriber3 = observablePair.subscribe( {output3.add(it)}, {}, {})

        then:
        output3.size() == 0

        when:
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        then:
        observablePair.pollCount == 3
        output1.size() == 1
        output2.size() == 2
        output3.size() == 1
    }
}
