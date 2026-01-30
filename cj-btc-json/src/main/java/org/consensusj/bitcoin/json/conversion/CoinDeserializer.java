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
package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bitcoinj.base.Coin;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Deserialize a numeric value to a bitcoinj {@link Coin} type.
 */
public class CoinDeserializer extends JsonDeserializer<Coin> {
    @Override
    public Coin deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {

            case VALUE_NUMBER_FLOAT:
                BigDecimal bd = p.getDecimalValue();
                return Coin.ofBtc(bd);

            case VALUE_NUMBER_INT:
                // TODO: Is this really what we want here? To treat JSON floats as BTC values and JSON integers as Satoshis?
                long val = p.getNumberValue().longValue(); // should be optimal, whatever it is
                return Coin.ofSat(val);

            // Read a numeric string. There are cases where a BTC decimal balance is encoded wrapped with quotes.
            case VALUE_STRING:
                return Coin.parseCoin(p.getText());

            default:
                return (Coin) ctxt.handleUnexpectedToken(Coin.class, p);
        }
    }
}
