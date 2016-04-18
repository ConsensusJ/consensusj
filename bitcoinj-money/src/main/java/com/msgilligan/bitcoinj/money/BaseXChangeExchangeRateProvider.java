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

import javax.money.CurrencyQuery;
import javax.money.CurrencyQueryBuilder;
import javax.money.CurrencyUnit;
import javax.money.NumberValue;
import javax.money.convert.ConversionContext;
import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import javax.money.convert.RateType;
import javax.money.spi.CurrencyProviderSpi;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *  Base ExchangeRateProvider using XChange library
 */
public abstract class BaseXChangeExchangeRateProvider extends BaseExchangeRateProvider {
    protected final ProviderContext providerContext;
    protected CurrencyProviderSpi bitcoinCurrencyProvider = new BitcoinCurrencyProvider();
    protected CurrencyUnit btc;
    protected String provider;
    protected Exchange exchange;
    protected PollingMarketDataService marketDataService;
    protected Ticker ticker;
    protected CurrencyPair btcusdPair;
    private ScheduledExecutorService stpe;
    private ScheduledFuture<?> future;
    private static final int initialDelay = 0;
    private static final int period = 60;

    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              CurrencyPair btcusdPair) {
        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClass.getName());
        this.btcusdPair = btcusdPair;
        provider = exchange.getExchangeSpecification().getExchangeName();
        providerContext = ProviderContext.of(provider, RateType.DEFERRED);
        marketDataService = exchange.getPollingMarketDataService();
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("BTC").build();
        Set<CurrencyUnit> currencies = bitcoinCurrencyProvider.getCurrencies(query);
        btc = (CurrencyUnit) currencies.toArray()[0];
        start();
    }

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

    protected void poll() {
        try {
            ticker = marketDataService.getTicker(btcusdPair);
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
        if (!(  conversionQuery.getBaseCurrency().getCurrencyCode().equals("BTC") &&
                conversionQuery.getCurrency().getCurrencyCode().equals("USD"))) {
            return null;
        }
        if (ticker == null) {
            return null;    // ticker not loaded yet (is returning null ok, here?)
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
