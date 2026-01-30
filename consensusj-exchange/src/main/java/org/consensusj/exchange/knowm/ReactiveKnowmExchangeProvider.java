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
package org.consensusj.exchange.knowm;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.consensusj.exchange.CurrencyUnitPair;
import org.consensusj.exchange.rx.ExchangeRateUpdate;
import org.consensusj.exchange.rx.PolledObservablePair;
import org.consensusj.exchange.rx.ReactiveExchange;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.money.CurrencyUnit;
import javax.money.convert.ProviderContext;
import javax.money.convert.RateType;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
public class ReactiveKnowmExchangeProvider implements ReactiveExchange {
    private static final Logger log = LoggerFactory.getLogger(ReactiveKnowmExchangeProvider.class);
    protected final String exchangeClassName;
    protected final Map<CurrencyUnit, String> tickerSymbolConversions;
    protected final Observable<Long> interval;
    protected final ExchangeSpecification exchangeSpecification;
    
    protected final Map<CurrencyUnitPair, MonitoredCurrency> observablePairs = new HashMap<>();

    protected String name;
    protected ProviderContext providerContext;
    protected MarketDataService marketDataService;

    private volatile boolean initialized = false;
    protected Exchange exchange;

    public ReactiveKnowmExchangeProvider(String exchangeClassName,
                                         Map<CurrencyUnit, String> tickerSymbolConversions,
                                         Collection<CurrencyUnitPair> pairs,
                                         Observable<Long> interval) {
        this(null, exchangeClassName, tickerSymbolConversions, pairs, interval);
    }

    public ReactiveKnowmExchangeProvider(ExchangeSpecification exchangeSpecification,
                                         Map<CurrencyUnit, String> tickerSymbolConversions,
                                         Collection<CurrencyUnitPair> pairs,
                                         Observable<Long> interval) {
        this(exchangeSpecification, null, tickerSymbolConversions, pairs, interval);
    }

    public ReactiveKnowmExchangeProvider(ExchangeSpecification exchangeSpecification,
                                          String exchangeClassName,
                                          Map<CurrencyUnit, String> tickerSymbolConversions,
                                          Collection<CurrencyUnitPair> pairs,
                                          Observable<Long> interval) {
        this.exchangeSpecification = exchangeSpecification;
        if (exchangeSpecification != null) {
            this.exchangeClassName = exchangeSpecification.getExchangeClassName();
        } else {
            this.exchangeClassName = exchangeClassName;
        }
        this.tickerSymbolConversions = (tickerSymbolConversions != null) ? tickerSymbolConversions : Map.of();
        this.interval = interval;
        for (CurrencyUnitPair pair : pairs) {
            MonitoredCurrency observablePair = new MonitoredCurrency(pair, xchangePair(pair));
            observablePairs.put(pair, observablePair);
        }
    }
    
    private synchronized void initialize()  {
        if (!initialized) {
            log.info("initializing, calling createExchange()");
            // createExchange w/o exchangeSpecification does a remote initialization that takes approximately 1.5 seconds
            if (exchangeSpecification != null) {
                exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
            } else {
                exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClassName);
            }
            log.info("initializing, getting name");
            name = exchange.getExchangeSpecification().getExchangeName();
            providerContext = ProviderContext.of(name, RateType.DEFERRED);
            marketDataService = exchange.getMarketDataService();
            initialized = true;
            log.info("initialized");
        }
        log.info("exiting");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PolledObservablePair getObservablePair(CurrencyUnitPair pair) {
        return observablePairs.get(pair).observable;
    }

    protected class MonitoredCurrency {
        final public CurrencyUnitPair  pair;        // Terminating (target) JavaMoney CurrencyUnit
        final public CurrencyPair exchangePair;     // XChange currency pair (format used by XChange/exchange)
        final public PolledObservablePair observable;
        
        MonitoredCurrency(CurrencyUnitPair pair, CurrencyPair exchangePair) {
            this.pair = pair;
            this.exchangePair = exchangePair;
            observable = new KnowmObservablePair(pair, this::pollingFunction, interval);
        }

        public ExchangeRateUpdate pollingFunction() {
            Ticker ticker = pollForTicker();
            return tickerMapper(ticker);
        }

        public Ticker pollForTicker() {
            try {
                return marketDataService.getTicker(exchangePair);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public ExchangeRateUpdate tickerMapper(Ticker ticker) {
            Date timestamp = ticker.getTimestamp();
            long time = (timestamp != null) ? timestamp.getTime() : 0;
            return new ExchangeRateUpdate(pair,
                    DefaultNumberValue.of(ticker.getBid()),
                    time);
        }
    }

    /**
     * Subclass PolledObservablePair to lazy initialize exchange on subscribe
     */
    private class KnowmObservablePair extends PolledObservablePair {

        /**
         * @param pair         The currency pair to observe
         * @param rateSupplier A polling Supplier function (for a single pair)
         * @param interval     An interval (or equivalent) to provide clock ticks to trigger polling
         */
        private KnowmObservablePair(CurrencyUnitPair pair, Supplier<ExchangeRateUpdate> rateSupplier, Observable<Long> interval) {
            super(pair, rateSupplier, interval);
        }


        @Override
        protected void subscribeActual(@NonNull Observer<? super ExchangeRateUpdate> observer) {
            initialize();
            super.subscribeActual(observer);
        }
    }


    /**
     * Map from CurrencyUnitPair to XChange CurrencyPair
     * tickerSymbolConversions is used to handle cases like ItBit that uses "XBT" instead of "BTC"
     * @param pair  CurrencyUnitPair using JavaMoney CurrencyUnits
     * @return  XChange CurrencyPair with exchange-specific symbols if any
     */
    protected CurrencyPair xchangePair(CurrencyUnitPair pair) {
        return new CurrencyPair(convertSymbol(pair.getBase()),
                convertSymbol(pair.getTarget()));
    }

    /**
     * Convert a JavaMoney CurrencyUnit to an XChange currency code string
     * (This will be exchange-specific (e.g. ItBit uses "XBT" instead of "BTC")
     * @param currencyUnit A JavaMoney currency unit
     * @return exchange-specific symbol for the currency
     */
    public String convertSymbol(CurrencyUnit currencyUnit) {
        return tickerSymbolConversions.getOrDefault(currencyUnit, currencyUnit.getCurrencyCode());
    }

}
