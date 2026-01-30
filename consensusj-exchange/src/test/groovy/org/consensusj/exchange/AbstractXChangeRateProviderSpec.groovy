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
package org.consensusj.exchange

import org.javamoney.moneta.Money
import spock.lang.Shared
import spock.lang.Specification

import javax.money.Monetary
import javax.money.MonetaryAmount


/**
 * Base class for testing exchange rate providers
 */
abstract class AbstractXChangeRateProviderSpec extends Specification {
    @Shared BaseXChangeExchangeRateProvider provider

    def "can get an exchange rate via currency strings"() {
        when:
        def rate = provider.getExchangeRate("BTC", "USD")

        then:
        rate.factor.numberValue(BigDecimal.class) > 0
    }

    def "can get an exchange rate via currencyunits"() {
        given:
        def btc = Monetary.getCurrency("BTC")
        def usd = Monetary.getCurrency("USD")

        when:
        def rate = provider.getExchangeRate(btc, usd)

        then:
        rate.factor.numberValue(BigDecimal.class) > 0
    }

    def "can get a working CurrencyConversion"() {
        when:
        def conversion = provider.getCurrencyConversion("USD")

        then:
        conversion != null

        when:
        MonetaryAmount amountInBTC = Money.of(1, "BTC")
        MonetaryAmount amountInUSD = amountInBTC.with(conversion)

        then:
        amountInUSD.number.doubleValue() > 400
        amountInUSD.currency.currencyCode == "USD"
    }

    def "can list currency codes"() {
        when:
        def markets = provider.exchange.exchangeMetaData.currencyPairs

        then:
        markets.size() > 0
    }

    def setupSpec() {
        // Note that creating a provider can create traffic to the API server
        // and can trigger rate limiting. Se we create the provider in setupSpec()
        // and not in setup()
        provider = createProvider()
        provider.start()
    }

    def cleanup() {
        def sleepTime = 100
        println "sleeping for ${sleepTime} milliseconds"
        sleep(sleepTime) // To avoid rate-limit problems from API providers
    }

    abstract BaseXChangeExchangeRateProvider createProvider()
}