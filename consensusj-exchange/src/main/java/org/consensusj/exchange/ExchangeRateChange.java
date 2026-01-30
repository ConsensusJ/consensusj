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
package org.consensusj.exchange;

import javax.money.convert.ExchangeRate;

/**
 * Data object passed to `ExchangeRateObserver` on rate updates
 * TODO: This should become a JavaMoney version of the XChange Ticker
 * It should have the JavaMoney currency types, all the rates,
 * An optional server timestamp and a mandatory client timestamp
 * TODO: And it should probably be called "Update" rather than "Change" -- if the timestamp
 * was updated but the exchange rate didn't, users still want to know.
 */
@Deprecated
public class ExchangeRateChange {
    public final ExchangeRate rate;
    public final Long timestamp;

    public ExchangeRateChange(ExchangeRate rate, long timestamp) {
        this.rate = rate;
        this.timestamp = timestamp;
    }
}
