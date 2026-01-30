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
package org.consensusj.currency

import org.javamoney.moneta.CurrencyUnitBuilder
import org.javamoney.moneta.Money
import spock.lang.Shared
import spock.lang.Specification

import javax.money.CurrencyQuery
import javax.money.CurrencyQueryBuilder
import javax.money.CurrencyUnit
import javax.money.Monetary
import javax.money.MonetaryAmount

/**
 * Spock test specification for BitcoinCurrencyProvider
 * @author Sean Gilligan
 */
class BitcoinCurrencyProviderSpec extends Specification  {

    @Shared BitcoinCurrencyProvider provider

    def setup() {
        provider = new BitcoinCurrencyProvider()
    }

    def "can create new instance"() {
        expect:
        provider != null
    }

    def "returns Bitcoin for empty query" () {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)
        CurrencyUnit btc = (CurrencyUnit) currencies.toArray()[0]

        then:
        currencies.size() == 1
        btc.getCurrencyCode() == "BTC"
        btc.getDefaultFractionDigits() == 8
    }

    def "returns Bitcoin for BTC query" () {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("BTC").build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)
        CurrencyUnit btc = (CurrencyUnit) currencies.toArray()[0]

        then:
        currencies.size() == 1
        btc.getCurrencyCode() == "BTC"
        btc.getDefaultFractionDigits() == 8
    }

    def "returns empty for USD query" () {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("USD").build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)

        then:
        currencies.size() == 0
    }

    def "BTC can be found via Monetary (via META-INF.services)" () {
        when: "We try to get currency 'BTC' via registered services"
        CurrencyUnit btc = Monetary.getCurrency("BTC")

        then: "we find it"
        btc.getCurrencyCode() == "BTC"
        btc.getDefaultFractionDigits() == 8
    }

    def "we can create money with Money.of"() {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("BTC").build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)
        CurrencyUnit btc = (CurrencyUnit) currencies.toArray()[0]
        MonetaryAmount oneBitcoin = Money.of(1, btc)

        then:
        oneBitcoin.number.longValueExact() == 1L
        oneBitcoin.currency == btc
    }

    def "we can create money with Money.of without using a provider"() {
        when:
        CurrencyUnit btc = CurrencyUnitBuilder.of("BTC", "ad hoc btc provider").build()
        MonetaryAmount oneBitcoin = Money.of(1, btc)

        then:
        oneBitcoin.number.longValueExact() == 1L
        oneBitcoin.currency == btc
    }
}
