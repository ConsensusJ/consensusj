package com.msgilligan.bitcoinj.money;

import com.msgilligan.currency.exchanges.BitfinexClient;
import org.javamoney.moneta.ExchangeRateBuilder;
import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.CurrencyQuery;
import javax.money.CurrencyQueryBuilder;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.NumberValue;
import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.ProviderContext;
import javax.money.convert.RateType;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Set;

/**
 * A converter for Bitfinex
 * Based on Werner Keil's BitcoinDeRateProvider
 */
public class BitfinexExchangeRateProvider implements ExchangeRateProvider {
    private BitfinexClient client;
    private CurrencyProviderSpi bitcoinCurrencyProvider = new BitcoinCurrencyProvider();
    private CurrencyUnit btc;

    public BitfinexExchangeRateProvider() {
        client = new BitfinexClient();
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("BTC").build();
        Set<CurrencyUnit> currencies = bitcoinCurrencyProvider.getCurrencies(query);
        btc = (CurrencyUnit) currencies.toArray()[0];
    }

    @Override
    public ProviderContext getContext() {
        return null;
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        return null;
    }

    @Override
    public CurrencyConversion getCurrencyConversion(ConversionQuery conversionQuery) {
        return null;
    }

    @Override
    public boolean isAvailable(ConversionQuery conversionQuery) {
        return false;
    }

    @Override
    public ExchangeRate getExchangeRate(CurrencyUnit base, CurrencyUnit term) {
        final NumberValue factor = DefaultNumberValue.of(client.getPrice());
        return new ExchangeRateBuilder("Bitfinex", RateType.DEFERRED)
                .setBase(base)
                .setTerm(term)
                .setFactor(factor)
                .build();
    }

    @Override
    public CurrencyConversion getCurrencyConversion(CurrencyUnit term) {
        return null;
    }

    @Override
    public boolean isAvailable(CurrencyUnit base, CurrencyUnit term) {
        return getExchangeRate(base, term) != null;
    }

    @Override
    public boolean isAvailable(String baseCode, String termCode) {
        return getExchangeRate(baseCode, termCode) != null;
    }

    @Override
    public ExchangeRate getExchangeRate(String baseCode, String termCode) {
        CurrencyUnit base = Monetary.getCurrency(baseCode);
//        CurrencyUnit term = Monetary.getCurrency(termCode);
        if (!termCode.equals("BTC")) {
            throw new UnsupportedOperationException("currency code must be BTC");
        }
        CurrencyUnit term = btc;
        return getExchangeRate(base, term);
    }

    @Override
    public ExchangeRate getReversed(ExchangeRate rate) {
        return null;
    }

    @Override
    public CurrencyConversion getCurrencyConversion(String termCode) {
        return null;
    }
}
