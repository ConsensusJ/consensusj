package com.msgilligan.bitcoinj.rpc.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.IOException;

/**
 *
 */
public class ECKeyDeserializer extends JsonDeserializer<ECKey> {
    @Override
    public ECKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    return new DumpedPrivateKey(null, p.getValueAsString()).getKey();
                } catch (AddressFormatException e) {
                    throw new InvalidFormatException("Invalid Key", p.getValueAsString(), ECKey.class);
                }
            default:
                throw ctxt.mappingException(Address.class, token);
        }
    }
}
