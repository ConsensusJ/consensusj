package com.msgilligan.bitcoinj.money;

import org.javamoney.moneta.ExchangeRateBuilder;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.javamoney.moneta.spi.LazyBoundCurrencyConversion;
import org.javamoney.moneta.spi.base.BaseExchangeRateProvider;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.polling.marketdata.PollingMarketDataService;

import javax.money.convert.ConversionContext;
import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import javax.money.convert.RateType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *  Base ExchangeRateProvider using XChange library
 *  Currently supports current DEFERRED rates only
 */
public abstract class BaseXChangeExchangeRateProvider extends BaseExchangeRateProvider
                                            implements ObservableExchangeRateProvider {
    protected final ProviderContext providerContext;
    protected String name;
    protected Exchange exchange;
    protected PollingMarketDataService marketDataService;
    private ScheduledExecutorService stpe;
    private ScheduledFuture<?> future;
    private final Map<CurrencyUnitPair, MonitoredCurrency> monitoredCurrencies = new HashMap<>();
    private static final int initialDelay = 0;
    private static final int period = 60;

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClass
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              CurrencyUnitPair... pairs) {
        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClass.getName());
        name = exchange.getExchangeSpecification().getExchangeName();
        providerContext = ProviderContext.of(name, RateType.DEFERRED);
        marketDataService = exchange.getPollingMarketDataService();
        for (CurrencyUnitPair pair : pairs) {
            MonitoredCurrency monitoredCurrency = new MonitoredCurrency(pair, xchangePair(pair));
            monitoredCurrencies.put(pair, monitoredCurrency);
        }
        start();    // starting here causes first ticker to be read before observers can be registered!!!
    }

    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              String... pairs) {
        this(exchangeClass, pairsConvert(pairs));
    }

    private static CurrencyUnitPair[] pairsConvert(String[] strings) {
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
    public void start() {
        stpe = Executors.newScheduledThreadPool(2);
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
    public void stop() {
        final ScheduledFuture<?> handle = future;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                handle.cancel(true);
            }
        };
        stpe.schedule(task, 0, TimeUnit.SECONDS);
        stpe.shutdown();
    }

    /**
     * Poll the exchange for updated Tickers
     */
    protected void poll() {
        for (Map.Entry<CurrencyUnitPair, MonitoredCurrency> entry : monitoredCurrencies.entrySet()) {
            try {
                MonitoredCurrency monitor = entry.getValue();
                monitor.setTicker(marketDataService.getTicker(monitor.exchangePair));
                notifyExchangeRateObservers(monitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerExchangeRateObserver(CurrencyUnitPair pair, ExchangeRateObserver observer) {
        // TODO: validate rate as one this provider supports
        MonitoredCurrency monitor = monitoredCurrencies.get(pair);
        monitor.observerList.add(observer);
    }

    public void notifyExchangeRateObservers(MonitoredCurrency monitor) {
        for (ExchangeRateObserver observer : monitor.observerList) {

            observer.notify(new ExchangeRateChange(buildExchangeRate(monitor), monitor.getTicker().getTimestamp().getTime()));
        }
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
            return null;
        }
        return buildExchangeRate(monitoredCurrency);
    }

    protected ExchangeRate buildExchangeRate(MonitoredCurrency monitoredCurrency) {
        return new ExchangeRateBuilder(name, RateType.DEFERRED)
                .setBase(monitoredCurrency.pair.getBase())
                .setTerm(monitoredCurrency.pair.getTarget())
                .setFactor(DefaultNumberValue.of(monitoredCurrency.getTicker().getLast()))
                .build();
    }

    @Override
    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
        return new LazyBoundCurrencyConversion(conversionQuery, this, ConversionContext
                .of(getContext().getProviderName(), getContext().getRateTypes().iterator().next()));
    }

    protected static class MonitoredCurrency {
        final CurrencyUnitPair  pair;           // Terminating (target) JavaMoney CurrencyUnit
        final CurrencyPair      exchangePair;   // XChange currency pair (format used by XChange/exchange)
        final List<ExchangeRateObserver> observerList = new ArrayList<>();
        private final CountDownLatch tickerReady = new CountDownLatch(1);
        private Ticker _ticker = null; // The '_' means use the getter and setter, please

        public MonitoredCurrency(CurrencyUnitPair pair, CurrencyPair exchangePair) {
            this.pair = pair;
            this.exchangePair = exchangePair;
        }

        Ticker getTicker() {
            if (_ticker == null) {
                // were we called before first poll completed?
                try {
                    // wait for the CountdownLatch
                    boolean ready = tickerReady.await(60, TimeUnit.SECONDS);
                    if (!ready) {
                        throw new RuntimeException("timeout");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return _ticker;
        }

        void setTicker(Ticker ticker) {
            _ticker = ticker;
            tickerReady.countDown();
        }
    }
}
