package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;

/**
 *
 */
public class AddressDeserializer extends JsonDeserializer<Address> {
    @Override
    public Address deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    return new Address(null, p.getValueAsString());
                } catch (AddressFormatException e) {
                    throw new InvalidFormatException("Invalid Address", p.getValueAsString(), Address.class);
                }
            default:
                throw ctxt.mappingException(Address.class, token);
        }
    }
}
