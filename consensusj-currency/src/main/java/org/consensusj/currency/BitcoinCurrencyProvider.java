package org.consensusj.currency;

import org.javamoney.moneta.CurrencyUnitBuilder;
import org.javamoney.moneta.spi.base.BaseCurrencyProviderSpi;

import javax.money.CurrencyContext;
import javax.money.CurrencyContextBuilder;
import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Collections;
import java.util.Set;

/**
 *  A BitcoinCurrencyProvider based on work in the javamoney-shelter.
 *
 * @author Sean Gilligan
 * @author Werner Keil
 */
public class BitcoinCurrencyProvider extends BaseCurrencyProviderSpi implements CurrencyProviderSpi {
    private final static int bitcoinFractionDigits = 8;
    private final Set<CurrencyUnit> bitcoinSet;

    public BitcoinCurrencyProvider() {
        CurrencyContext CONTEXT = CurrencyContextBuilder.of("BitcoinCurrencyContextProvider")
                .build();
        CurrencyUnit btcUnit = CurrencyUnitBuilder.of("BTC", CONTEXT)
                .setDefaultFractionDigits(bitcoinFractionDigits)
                .build();
        bitcoinSet = Collections.singleton(btcUnit);
    }
    
    /**
     * Return a {@link CurrencyUnit} instances matching the given
     * {@link javax.money.CurrencyQuery}.
     *
     * @param query the {@link javax.money.CurrencyQuery} containing the parameters determining the query. not null.
     * @return the corresponding {@link CurrencyUnit}s matching, never null.
     */
    @Override
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery query){
        // Query for currencyCode BTC or default query returns bitcoinSet else emptySet.
        return (query.getCurrencyCodes().contains("BTC") ||
                query.getCurrencyCodes().isEmpty()) ? bitcoinSet : Collections.emptySet();
    }
}