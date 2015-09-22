package com.msgilligan.bitcoinj.rpc.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bitcoinj.core.Coin;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 */
public class CoinDeserializer extends JsonDeserializer<Coin> {
    @Override
    public Coin deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {

            case VALUE_NUMBER_FLOAT:
                BigDecimal bd = p.getDecimalValue();
                return BitcoinMath.btcToCoin(bd);

            case VALUE_NUMBER_INT:
                long val = p.getNumberValue().longValue(); // should be optimal, whatever it is
                return Coin.valueOf(val);

            default:
                throw ctxt.mappingException(Coin.class, token);
        }
    }
}
