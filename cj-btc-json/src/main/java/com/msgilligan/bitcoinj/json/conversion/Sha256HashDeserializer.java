package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Deserialize a hex string to a bitcoinj {@link Sha256Hash} type.
 */
public class Sha256HashDeserializer  extends JsonDeserializer<Sha256Hash> {
    @Override
    public Sha256Hash deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                return Sha256Hash.wrap(p.getValueAsString());
            default:
                return (Sha256Hash) ctxt.handleUnexpectedToken(Sha256Hash.class, p);
        }
    }
}
