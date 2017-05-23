package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;

/**
 * Key Deserializer to support using Address as the key in a Map.
 */
public class AddressKeyDeserializer extends KeyDeserializer {
    private final NetworkParameters netParams;

    public AddressKeyDeserializer(NetworkParameters netParams) {
        this.netParams = netParams;
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Address.fromBase58(netParams, key);
    }
}
