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
package org.consensusj.currency;

import org.javamoney.moneta.CurrencyUnitBuilder;

import javax.money.CurrencyContext;
import javax.money.CurrencyContextBuilder;
import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Set;

/**
 *  A BitcoinCurrencyProvider based on work in the javamoney-shelter.
 *
 * @author Sean Gilligan
 * @author Werner Keil
 */
public class BitcoinCurrencyProvider implements CurrencyProviderSpi {
    private final static int bitcoinFractionDigits = 8;

    private static final CurrencyContext CONTEXT = CurrencyContextBuilder.of("BitcoinCurrencyContextProvider")
            .build();

    private final Set<CurrencyUnit> bitcoinSet;

    public BitcoinCurrencyProvider() {
        CurrencyUnit btcUnit = CurrencyUnitBuilder.of("BTC", CONTEXT)
                .setDefaultFractionDigits(bitcoinFractionDigits)
                .build();
        bitcoinSet = Set.of(btcUnit);
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
                query.getCurrencyCodes().isEmpty()) ? bitcoinSet : Set.of();
    }
}