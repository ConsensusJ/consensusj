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
package org.consensusj.exchange.rx;

import org.consensusj.exchange.CurrencyUnitPair;
import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.NumberValue;

/**
 * 
 */
public class ExchangeRateUpdate {
    public final CurrencyUnitPair pair;
    public final NumberValue currentFactor;
    public final long serverTimeStamp;
    //public final long clientTimeStamp; // ??
    //ExchangeRate rate;  // Should we have this?
    //Other "Ticker" information?
    
    public ExchangeRateUpdate(CurrencyUnitPair pair, NumberValue currentFactor, long serverTimeStamp) {
        this.pair = pair;
        this.currentFactor = currentFactor;
        this.serverTimeStamp = serverTimeStamp;
    }

    /**
     * Create an ExchangeRateValue for "unknown" exchange rate (no data yet)
     *
     * @param pair Exchange pair we don't have information on
     * @return An ExchangeRateUpdate with "unknown" values
     */
    public static ExchangeRateUpdate unknown(CurrencyUnitPair pair) {
        return new ExchangeRateUpdate(pair, DefaultNumberValue.of(0), 0);
    }
}
