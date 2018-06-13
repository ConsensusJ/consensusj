package com.msgilligan.bitcoinj.money;

import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *  Base ExchangeRateProvider using XChange library
 *  Currently supports current DEFERRED rates only
 */
public abstract class BaseXChangeExchangeRateProvider implements ExchangeRateProvider, ObservableExchangeRateProvider {
    private static final Logger log = LoggerFactory.getLogger(BaseXChangeExchangeRateProvider.class);
    protected final ProviderContext providerContext;
    protected String name;
    protected Exchange exchange;
    protected MarketDataService marketDataService;
    private ScheduledExecutorService stpe;
    private ScheduledFuture<?> future;
    private final Map<CurrencyUnitPair, MonitoredCurrency> monitoredCurrencies = new HashMap<>();
    private static final int initialDelay = 0;
    private static final int period = 60;
    private volatile boolean stopping = false;

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClassName Classname of XChange exchange we are wrapping
     * @param scheduledExecutorService Executor service for running polling task
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(String exchangeClassName,
                                              ScheduledExecutorService scheduledExecutorService,
                                              CurrencyUnitPair... pairs) {
        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClassName);
        stpe = (scheduledExecutorService != null) ? scheduledExecutorService : Executors.newScheduledThreadPool(1);
        name = exchange.getExchangeSpecification().getExchangeName();
        providerContext = ProviderContext.of(name, RateType.DEFERRED);
        marketDataService = exchange.getMarketDataService();
        for (CurrencyUnitPair pair : pairs) {
            MonitoredCurrency monitoredCurrency = new MonitoredCurrency(pair, xchangePair(pair));
            monitoredCurrencies.put(pair, monitoredCurrency);
        }
        start();    // starting here causes first ticker to be read before observers can be registered!!!
    }

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClass Class of XChange exchange we are wrapping
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              CurrencyUnitPair... pairs) {
        this(exchangeClass,
                null,
                pairs);
    }

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClass Class of XChange exchange we are wrapping
     * @param scheduledExecutorService Executor service for running polling task
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              ScheduledExecutorService scheduledExecutorService,
                                              CurrencyUnitPair... pairs) {
        this(exchangeClass.getName(), scheduledExecutorService, pairs);
    }

    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              ScheduledExecutorService scheduledExecutorService,
                                              String... pairs) {
        this(exchangeClass, scheduledExecutorService, pairsConvert(pairs));
    }

    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              String... pairs) {
        this(exchangeClass, pairsConvert(pairs));
    }

    protected static CurrencyUnitPair[] pairsConvert(String[] strings) {
        CurrencyUnitPair[] units = new CurrencyUnitPair[strings.length];
        for (int i = 0 ; i < strings.length ; i++) {
            units[i] = new CurrencyUnitPair(strings[i]);
        }
        return units;
    }

    /**
     * Map from CurrencyUnitPair to XChange CurrencyPair
     * Override to handle cases like ItBit that use "XBT" instead of "BTC"
     * @param pair  CurrencyUnitPair using JavaMoney CurrencyUnits
     * @return  XChange CurrencyPair
     */
    protected CurrencyPair xchangePair(CurrencyUnitPair pair) {
        return new CurrencyPair(pair.getBase().getCurrencyCode(), pair.getTarget().getCurrencyCode());
    }

    /**
     * Start the polling thread
     */
    @Override
    public void start() {
        final BaseXChangeExchangeRateProvider that = this;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                that.poll();
            }
        };
        future = stpe.scheduleWithFixedDelay(task, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * stop the polling thread
     */
    @Override
    public void stop() {
        if (!stopping) {
            stopping = true;
            final ScheduledFuture<?> handle = future;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    handle.cancel(true);
                }
            };
            stpe.schedule(task, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Poll the exchange for updated Tickers
     */
    protected void poll() {
        try {
            for (Map.Entry<CurrencyUnitPair, MonitoredCurrency> entry : monitoredCurrencies.entrySet()) {
                MonitoredCurrency monitor = entry.getValue();
                monitor.setTicker(marketDataService.getTicker(monitor.exchangePair));
                notifyExchangeRateObservers(monitor);
            }
        } catch (IOException e) {
            // log and ignore IOException (we'll try polling again next interval)
            log.error("IOException in BaseXChangeExchangeRateProvider::poll: {}", e);
        } catch (Throwable e) {
            // log and rethrow others
            log.error("Exception in BaseXChangeExchangeRateProvider::poll: {}", e);
            throw e;
        }
    }

    @Override
    public void registerExchangeRateObserver(CurrencyUnitPair pair, ExchangeRateObserver observer) {
        // TODO: validate rate as one this provider supports
        MonitoredCurrency monitor = monitoredCurrencies.get(pair);
        monitor.observerList.add(observer);
        // If we've got data already, call observer immediately
        if (monitor.isTickerAvailable()) {
            notifyObserver(observer, pair, monitor);
        }
    }

    private void notifyExchangeRateObservers(MonitoredCurrency monitor) {
        for (ExchangeRateObserver observer : monitor.observerList) {
            notifyObserver(observer, monitor.pair, monitor);
        }
    }

    private void notifyObserver(ExchangeRateObserver observer, CurrencyUnitPair pair, MonitoredCurrency monitor) {
        try {
            observer.onExchangeRateChange(buildExchangeRateChange(pair, monitor.getTicker()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interruption flag just in case
        } catch (TimeoutException e) {
            throw new RuntimeException(e);      // Unlikely to happen since ticker has usually been fetched
        }
    }

    private void notifyObserver(ExchangeRateObserver observer, CurrencyUnitPair pair, Ticker ticker) {
        observer.onExchangeRateChange(buildExchangeRateChange(pair, ticker));
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
        MonitoredCurrency monitoredCurrency = monitoredCurrencies.get(pair);
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

    private ExchangeRateChange buildExchangeRateChange(CurrencyUnitPair pair, Ticker ticker) {
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

    protected static class MonitoredCurrency {
        final CurrencyUnitPair  pair;           // Terminating (target) JavaMoney CurrencyUnit
        final CurrencyPair      exchangePair;   // XChange currency pair (format used by XChange/exchange)
        final List<ExchangeRateObserver> observerList = new ArrayList<>();
        private final UpdateableValueLatch<Ticker> tickerLatch = new UpdateableValueLatch<>();

        MonitoredCurrency(CurrencyUnitPair pair, CurrencyPair exchangePair) {
            this.pair = pair;
            this.exchangePair = exchangePair;
        }

        boolean isTickerAvailable() {
            return tickerLatch.isSet();
        }

        /**
         * Get Ticker from the UpdateableValueLatch
         * @return ticker object
         * @throws InterruptedException thread was interrupted by another thread
         * @throws TimeoutException should only happen if first request times out
         */
        Ticker getTicker() throws InterruptedException, TimeoutException {
            return tickerLatch.getValue();
        }

        /**
         * Set the ticker object, called from polling method
         * @param ticker ticker object
         */
        void setTicker(Ticker ticker) {
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
