/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.exchange.rx

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.consensusj.exchange.CurrencyUnitPair
import org.javamoney.moneta.spi.DefaultNumberValue
import spock.lang.Shared
import spock.lang.Specification

import javax.money.NumberValue
import static java.util.concurrent.TimeUnit.SECONDS
import java.util.function.Supplier

/**
 *
 */
class ObservablePairSpec extends Specification {
    static final CurrencyUnitPair identityPair = new CurrencyUnitPair("USD", "USD")
    static final NumberValue fixedRate = DefaultNumberValue.of(1)
    static final ExchangeRateUpdate fixedUpdate = new ExchangeRateUpdate(identityPair, fixedRate, 0)
    static final Supplier<ExchangeRateUpdate> fixedSupplier = { -> fixedUpdate } as Supplier<ExchangeRateUpdate>
    
    @Shared
    Integer counter
    
    @Shared
    Supplier<ExchangeRateUpdate> supplier

    def "Quick test with fixed, cold sequence as interval"() {
        given: "An observable pair with a finite interval sequence"
        def sequence = [0L,0L,0L]
        def observablePair = new PolledObservablePair(identityPair, supplier, Observable.fromIterable(sequence))

        when: "we subscribe"
        def output = [] as List<ExchangeRateUpdate>
        def disposable = observablePair.subscribe({ update -> output.add(update) })

        then: "we immediately get all the expected output and completion/disposal "
        counter == sequence.size()
        output.size() == sequence.size()
        output.every { it.pair == identityPair }
        output.every { it.currentFactor == fixedRate }
        output.every { it.serverTimeStamp == 0 }
        disposable.isDisposed()
    }

    def "Second subscriber gets immediate cached data if first subscriber remains subscribed"() {
        given:
        def scheduler = new TestScheduler()
        def observablePair = new PolledObservablePair(identityPair, supplier, Observable.interval(1, SECONDS, scheduler))
        def output1 = [] as List<ExchangeRateUpdate>
        def output2 = [] as List<ExchangeRateUpdate>

        when:
        def subscriber1 = observablePair.subscribe({ output1.add(it) }, {}, {})
        scheduler.advanceTimeBy(2, SECONDS)

        then:
        output1.size() == 2

        when:
        def subscriber2 = observablePair.subscribe({ output2.add(it) }, {}, {})
        scheduler.triggerActions()

        then:
        output2.size() == 1
    }

    def "Second subscriber DOESN'T get immediate cached data after first subscriber unsubscribes"() {
        given:
        def scheduler = new TestScheduler()
        def observablePair = new PolledObservablePair(identityPair, supplier, Observable.interval(1, SECONDS, scheduler))
        def output1 = [] as List<ExchangeRateUpdate>
        def output2 = [] as List<ExchangeRateUpdate>

        when:
        def subscriber1 = observablePair.subscribe({ output1.add(it) }, {}, {})
        scheduler.advanceTimeBy(2, SECONDS)
        subscriber1.dispose()

        then:
        output1.size() == 2

        when:
        def subscriber2 = observablePair.subscribe({ output2.add(it) }, {}, {})
        scheduler.triggerActions()

        then:
        output2.size() == 0 // This is the behavior of replay(1).refCount() but not really what we want
    }

    def "Test with interval and TestScheduler"() {
        given:
        def scheduler = new TestScheduler()
        def observablePair = new PolledObservablePair(identityPair, supplier, Observable.interval(1, SECONDS, scheduler))
        def output1 = [] as List<ExchangeRateUpdate>
        def output2 = [] as List<ExchangeRateUpdate>
        def output3 = [] as List<ExchangeRateUpdate>

        when:
        def subscriber1 = observablePair.subscribe( {output1.add(it)}, {}, {})
        def subscriber2 = observablePair.subscribe( {output2.add(it)}, {}, {})

        then:
        output1.size() == 0
        output2.size() == 0
        counter == 0
        !subscriber1.isDisposed()
        !subscriber2.isDisposed()

        when:
        scheduler.advanceTimeBy(1, SECONDS)

        then:
        output1.size() == 1
        output2.size() == 1
        counter == 1
        !subscriber1.isDisposed()
        !subscriber2.isDisposed()

        when:
        subscriber1.dispose()
        scheduler.advanceTimeBy(1, SECONDS)

        then:
        output1.size() == 1
        output2.size() == 2
        counter == 2
        subscriber1.isDisposed()
        !subscriber2.isDisposed()

        when:
        subscriber2.dispose()
        scheduler.advanceTimeBy(5, SECONDS)

        then:
        counter == 2

        when:
        def subscriber3 = observablePair.subscribe( {output3.add(it)}, {}, {})
        scheduler.triggerActions()      // Give scheduler a tiny time slice to process the subscribe

        then:
        output3.size() == 0

        when:
        scheduler.advanceTimeBy(1, SECONDS)

        then:
        counter == 3
        output1.size() == 1
        output2.size() == 2
        output3.size() == 1

        when:
        subscriber3.dispose()
        scheduler.advanceTimeBy(5, SECONDS)

        then:
        subscriber3.isDisposed()
        counter == 3


    }

    def setup() {
        counter = 0
        supplier = { ->
            counter++
            fixedSupplier.get()
        }
    }
}
