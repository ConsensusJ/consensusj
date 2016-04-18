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
        poll();
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
