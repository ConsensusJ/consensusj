package org.consensusj.exchange.knowm

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.consensusj.exchange.CurrencyUnitPair
import org.consensusj.exchange.rx.ExchangeRateUpdate
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS

/**
 *
 */
class ReactiveKnowmExchangeProviderSpec extends Specification {
    static final CurrencyUnitPair btcUsdPair = new CurrencyUnitPair("BTC", "USD")

    def "smoke"() {
        given:
        def scheduler = new TestScheduler()
        def exchange = new ReactiveKnowmExchangeProvider("org.knowm.xchange.bitfinex.BitfinexExchange",
                null,
                [btcUsdPair],
                Observable.interval(1, SECONDS, scheduler));
        def observablePair = exchange.getObservablePair(btcUsdPair);
        def output1 = [] as List<ExchangeRateUpdate>

        when:
        def subscriber1 = observablePair.subscribe({ output1.add(it) }, {
            it -> println "error ${it}"},
                { println "complete"})
        scheduler.advanceTimeBy(1, SECONDS)

        then:
        output1.size() == 1
        output1.get(0).currentFactor > 1;
    }
}

