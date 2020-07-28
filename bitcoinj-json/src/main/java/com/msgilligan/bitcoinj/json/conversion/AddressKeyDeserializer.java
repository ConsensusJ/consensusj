package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;

/**
 * Key Deserializer to support using Address as the key in a Map.
 */
public class AddressKeyDeserializer extends KeyDeserializer {
    private final NetworkParameters netParams;

    /**
     * Construct an address deserializer that will deserialize addresses for any supported network.
     * See {@link NetworkParameters} to understand what the supported networks are.
     */
    public AddressKeyDeserializer() {
        this(null);
    }

    /**
     * Construct an address KeyDeserializer that validates addresses for the specified {@link NetworkParameters}.
     * When deserializing addresses, addresses that are not from the specified network will cause a
     * {@link InvalidFormatException} to be thrown during deserialization.
     *
     * @param netParams Network parameters to specify the only network we will deserialize addresses for.
     */
    public AddressKeyDeserializer(NetworkParameters netParams) {
        this.netParams = netParams;
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Address.fromString(netParams, key);
    }
}
