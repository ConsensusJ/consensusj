package org.consensusj.exchange.knowm;

import org.consensusj.exchange.CurrencyUnitPair;
import org.consensusj.exchange.ExchangeRateChange;
import org.consensusj.exchange.ExchangeRateObserver;
import org.consensusj.exchange.rx.ExchangeRateUpdate;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;
import org.javamoney.moneta.spi.base.BaseExchangeRateProvider;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;

import javax.money.CurrencyUnit;
import javax.money.MonetaryException;
import javax.money.convert.ConversionContext;
import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.CurrencyConversionException;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.ProviderContext;
import javax.money.convert.RateType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class KnowmExchangeRateProvider  extends BaseExchangeRateProvider implements ExchangeRateProvider {
    protected final String exchangeClassName;
    protected final Map<CurrencyUnit, String> tickerSymbolConversions;
    protected final Map<CurrencyUnitPair, MonitoredCurrency> monitoredCurrencies = new HashMap<>();

    protected String name;
    protected ProviderContext providerContext;
    protected MarketDataService marketDataService;

    private volatile boolean initialized = false;
    protected Exchange exchange;

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClassName Classname of XChange exchange we are wrapping
     * @param pairs pairs to monitor
     */
    protected KnowmExchangeRateProvider(String exchangeClassName,
                                              Map<CurrencyUnit, String> tickerSymbolConversions,
                                              Collection<CurrencyUnitPair> pairs) {
        this.exchangeClassName = exchangeClassName;
        this.tickerSymbolConversions = (tickerSymbolConversions != null) ? tickerSymbolConversions : new HashMap<>();
        for (CurrencyUnitPair pair : pairs) {
            KnowmExchangeRateProvider.MonitoredCurrency monitoredCurrency = new KnowmExchangeRateProvider.MonitoredCurrency(pair, xchangePair(pair));
            monitoredCurrencies.put(pair, monitoredCurrency);
        }
    }


    public synchronized void initialize()  {
        if (!initialized) {
            initialized = true;
            exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClassName);
            name = exchange.getExchangeSpecification().getExchangeName();
            providerContext = ProviderContext.of(name, RateType.DEFERRED);
            marketDataService = exchange.getMarketDataService();
        }
    }

    public ExchangeRateUpdate getUpdate(CurrencyUnitPair pair) {
        MonitoredCurrency pairMonitor = monitoredCurrencies.get(pair);
        Ticker ticker;
        try {
            ticker = marketDataService.getTicker(pairMonitor.exchangePair);
        } catch (IOException e) {
            e.printStackTrace();
            ticker = null;
        }
        return tickerToUpdate(pair, ticker);
    }

    private ExchangeRateUpdate tickerToUpdate(CurrencyUnitPair pair, Ticker ticker) {
        return (ticker != null) ?
                new ExchangeRateUpdate(pair, DefaultNumberValue.of(ticker.getBid()), ticker.getTimestamp().getTime()) :
                new ExchangeRateUpdate(pair, null, 0);
    }

    @Override
    public ProviderContext getContext() {
        return providerContext;
    }

    @Override
    public boolean isAvailable(ConversionQuery conversionQuery) {
        return getExchangeRate(conversionQuery) != null;
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        CurrencyUnitPair pair = new CurrencyUnitPair(conversionQuery.getBaseCurrency(), conversionQuery.getCurrency());
        KnowmExchangeRateProvider.MonitoredCurrency monitoredCurrency = monitoredCurrencies.get(pair);
        if (monitoredCurrency == null) {
            throw new CurrencyConversionException(pair.getBase(),
                    pair.getTarget(),
                    null,
                    "Pair not found.");
        }
        ExchangeRate rate = null;
        try {
            rate = buildExchangeRate(pair, monitoredCurrency.getTicker());
        } catch (TimeoutException e) {
            throw new MonetaryException("Timeout loading exchange rate", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interruption flag
        }
        return rate;
    }

    @Override
    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
        return new LazyBoundCurrencyConversion(conversionQuery, this, ConversionContext
                .of(getContext().getProviderName(), getContext().getRateTypes().iterator().next()));
    }

    protected ExchangeRateChange buildExchangeRateChange(CurrencyUnitPair pair, Ticker ticker) {
        Date date = ticker.getTimestamp();
        // Not all exchanges provide a timestamp, default to 0 if it is null
        long milliseconds = (date != null) ? date.getTime() : 0;

        return new ExchangeRateChange(buildExchangeRate(pair, ticker), milliseconds);
    }

    private ExchangeRate buildExchangeRate(CurrencyUnitPair pair, Ticker ticker) {
        return new ExchangeRateBuilder(name, RateType.DEFERRED)
                .setBase(pair.getBase())
                .setTerm(pair.getTarget())
                .setFactor(DefaultNumberValue.of(ticker.getLast()))
                .build();
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

    protected static class MonitoredCurrency {
        final public CurrencyUnitPair  pair;           // Terminating (target) JavaMoney CurrencyUnit
        final public CurrencyPair      exchangePair;   // XChange currency pair (format used by XChange/exchange)
        final public List<ExchangeRateObserver> observerList = new ArrayList<>();
        private final UpdateableValueLatch<Ticker> tickerLatch = new UpdateableValueLatch<>();

        MonitoredCurrency(CurrencyUnitPair pair, CurrencyPair exchangePair) {
            this.pair = pair;
            this.exchangePair = exchangePair;
        }

        public boolean isTickerAvailable() {
            return tickerLatch.isSet();
        }

        /**
         * Get Ticker from the UpdateableValueLatch
         * @return ticker object
         * @throws InterruptedException thread was interrupted by another thread
         * @throws TimeoutException should only happen if first request times out
         */
        public Ticker getTicker() throws InterruptedException, TimeoutException {
            return tickerLatch.getValue();
        }

        /**
         * Set the ticker object, called from polling method
         * @param ticker ticker object
         */
        public void setTicker(Ticker ticker) {
            tickerLatch.setValue(ticker);
        }
    }

    /**
     * A value latch that will block if value not yet set, but allows
     * the value to be updated regularly (e.g. when polling for new values)
     * @param <T>  Type of value to latch
     */
    static class UpdateableValueLatch<T> {
        private volatile T value = null;
        private final CountDownLatch ready = new CountDownLatch(1);
        private final long timeoutSeconds;

        UpdateableValueLatch() {
            this(120);
        }

        UpdateableValueLatch(long timeoutInSeconds) {
            this.timeoutSeconds = timeoutInSeconds;
        }

        T getValue() throws InterruptedException, TimeoutException {
            // were we called before first poll completed?
            if (value == null) {
                // wait for the CountDownLatch
                boolean loaded = ready.await(timeoutSeconds, TimeUnit.SECONDS);
                if (!loaded) {
                    throw new TimeoutException("Timeout before value set");
                }
            }
            return value;
        }

        /* synchronized ? */ void setValue(T newValue) {
            value = newValue;
            ready.countDown();
        }

        boolean isSet() {
            return value != null;
        }
    }
}
