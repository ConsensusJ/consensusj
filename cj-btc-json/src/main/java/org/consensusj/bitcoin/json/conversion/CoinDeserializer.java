package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bitcoinj.core.Coin;

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
