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

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.NumberValue;
import javax.money.convert.ConversionContext;
import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import javax.money.convert.RateType;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *  Base ExchangeRateProvider using XChange library
 *  Currently limited to a single conversion per instance
 */
public abstract class BaseXChangeExchangeRateProvider extends BaseExchangeRateProvider {
    protected final ProviderContext providerContext;
    protected String provider;
    protected Exchange exchange;
    protected PollingMarketDataService marketDataService;
    protected CountDownLatch tickerReady = new CountDownLatch(1);
    protected Ticker ticker;
    protected CurrencyPair pair;    //
    protected CurrencyUnit base;    // JavaMoney CurrencyUnit (e.g. Will be "BTC" for ItBit)
    protected CurrencyUnit term;
    private ScheduledExecutorService stpe;
    private ScheduledFuture<?> future;
    private static final int initialDelay = 0;
    private static final int period = 60;

    /**
     * Construct using an XChange Exchange class object and a single currency pair
     * @param exchangeClass
     * @param pair XChange CurrencyPair (e.g. Wll have code "XBT" for ItBit)
     * @param base JavaMoney CurrencyUnit (e.g. Will be "BTC" for ItBit)
     * @param term Target currency type (typically a fiat currency like "USD")
     */
    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              CurrencyPair pair, String base, String term) {
        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClass.getName());
        this.pair = pair;
        this.base = Monetary.getCurrency(base);
        this.term = Monetary.getCurrency(term);
        provider = exchange.getExchangeSpecification().getExchangeName();
        providerContext = ProviderContext.of(provider, RateType.DEFERRED);
        marketDataService = exchange.getPollingMarketDataService();
        start();
    }

    /**
     * Start the polling thread
     */
    protected void start() {
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
     * Poll the exchange for an updated Ticker
     */
    protected void poll() {
        try {
            ticker = marketDataService.getTicker(pair);
            tickerReady.countDown();
        } catch (IOException e) {
            e.printStackTrace();
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
        if (!(  conversionQuery.getBaseCurrency().getCurrencyCode().equals(base.getCurrencyCode()) &&
                conversionQuery.getCurrency().getCurrencyCode().equals(term.getCurrencyCode())) ) {
            return null;
        }
        if (ticker == null) {
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
        final NumberValue factor = DefaultNumberValue.of(ticker.getLast());
        return new ExchangeRateBuilder(provider, RateType.DEFERRED)
                .setBase(conversionQuery.getBaseCurrency())
                .setTerm(conversionQuery.getCurrency())
                .setFactor(factor)
                .build();
    }

    @Override
    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
        return new LazyBoundCurrencyConversion(conversionQuery, this, ConversionContext
                .of(getContext().getProviderName(), getContext().getRateTypes().iterator().next()));
    }

}
