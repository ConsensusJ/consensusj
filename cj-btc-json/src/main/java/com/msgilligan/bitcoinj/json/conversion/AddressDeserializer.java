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

import java.io.IOException;

/**
 * Deserialize bitcoinj (family) addresses
 */
public class AddressDeserializer extends JsonDeserializer<Address> {
    private final NetworkParameters netParams;

    /**
     * Construct an address deserializer that will deserialize addresses for any supported network.
     * See {@link NetworkParameters} to understand what the supported networks are.
     */
    public AddressDeserializer() {
        this(null);
    }

    /**
     * Construct an address deserializer that validates addresses for the specified {@link NetworkParameters}.
     * When deserializing addresses, addresses that are not from the specified network will cause a
     * {@link InvalidFormatException} to be thrown during deserialization.
     *
     * @param netParams Network parameters to specify the only network we will deserialize addresses for.
     */
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
