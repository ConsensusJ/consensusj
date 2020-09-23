package org.consensusj.exchange.knowm

import groovy.util.logging.Slf4j
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.consensusj.exchange.CurrencyUnitPair
import org.consensusj.exchange.rx.ExchangeRateUpdate
import org.knowm.xchange.ExchangeSpecification
import spock.lang.Ignore
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS

/**
 *
 */
@Ignore("Integration tests that talk to exchanges")
@Slf4j
class ReactiveKnowmExchangeProviderSpec extends Specification {
    static final CurrencyUnitPair btcUsdPair = new CurrencyUnitPair("BTC/USD")

    def "bitfinex smoke test"() {
        given:
        log.info "smoke/given"
        def scheduler = new TestScheduler()
        def exchange = new ReactiveKnowmExchangeProvider(getBitfinexExchangeSpecification("org.knowm.xchange.bitfinex.BitfinexExchange"),
                null,
                [btcUsdPair],
                Observable.interval(1, SECONDS, scheduler));
        def observablePair = exchange.getObservablePair(btcUsdPair);
        def output1 = [] as List<ExchangeRateUpdate>

        when:
        log.info "smoke/when"
        def subscriber1 = observablePair.subscribe(
                {
                    log.info "smoke/data"
                    output1.add(it)
                },
                {
            it -> println "error ${it}"
                },
                {
                    log.info "smoke/complete"
                })
        log.info "smoke/when/subscribe"
        scheduler.advanceTimeBy(1, SECONDS)
        log.info "smoke/when/after tick"
        
        then:
        output1.size() == 1
        output1.get(0).currentFactor > 1;
    }

    def "bittrex smoke test"() {
        given:
        log.info "smoke/given"
        def scheduler = new TestScheduler()
        def exchange = new ReactiveKnowmExchangeProvider(getBittrexExchangeSpecification("org.knowm.xchange.bittrex.BittrexExchange"),
                null,
                [btcUsdPair],
                Observable.interval(1, SECONDS, scheduler));
        def observablePair = exchange.getObservablePair(btcUsdPair);
        def output1 = [] as List<ExchangeRateUpdate>

        when:
        log.info "smoke/when"
        def subscriber1 = observablePair.subscribe(
                {
                    log.info "smoke/data"
                    output1.add(it)
                },
                {
                    it -> println "error ${it}"
                },
                {
                    log.info "smoke/complete"
                })
        log.info "smoke/when/subscribe"
        scheduler.advanceTimeBy(1, SECONDS)
        log.info "smoke/when/after tick"

        then:
        output1.size() == 1
        output1.get(0).currentFactor > 1;
    }

    public ExchangeSpecification getBitfinexExchangeSpecification(String exchangeClassName) {

        ExchangeSpecification exchangeSpecification =
                new ExchangeSpecification(exchangeClassName);
        exchangeSpecification.setSslUri("https://api.bitfinex.com/");
        exchangeSpecification.setHost("api.bitfinex.com");
        exchangeSpecification.setPort(80);
        exchangeSpecification.setExchangeName("BitFinex");
        exchangeSpecification.setExchangeDescription("BitFinex is a bitcoin exchange.");

        // MSG added ->

        exchangeSpecification.setShouldLoadRemoteMetaData(false);

        return exchangeSpecification;
    }

    public ExchangeSpecification getBittrexExchangeSpecification(String exchangeClassName) {

        ExchangeSpecification exchangeSpecification =
                new ExchangeSpecification(exchangeClassName);
        exchangeSpecification.setSslUri("https://bittrex.com/api/");
        exchangeSpecification.setExchangeSpecificParametersItem(
                "rest.v3.url", "https://api.bittrex.com/");
        exchangeSpecification.setHost("bittrex.com");
        exchangeSpecification.setPort(80);
        exchangeSpecification.setExchangeName("Bittrex");
        exchangeSpecification.setExchangeDescription("Bittrex is a bitcoin and altcoin exchange.");

        // MSG added ->

        exchangeSpecification.setShouldLoadRemoteMetaData(false);

        return exchangeSpecification;
    }


}

