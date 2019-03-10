package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;

/**
 *
 */
public class AddressDeserializer extends JsonDeserializer<Address> {
    private NetworkParameters netParams;

    public AddressDeserializer(NetworkParameters netParams) {
        this.netParams = netParams;
    }

    @Override
    public Address deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    return Address.fromString(netParams, p.getValueAsString());
                } catch (AddressFormatException e) {
                    throw new InvalidFormatException(p, "Invalid Address", p.getValueAsString(), Address.class);
                }
            default:
                return (Address) ctxt.handleUnexpectedToken(Address.class, p);
        }
    }
}
