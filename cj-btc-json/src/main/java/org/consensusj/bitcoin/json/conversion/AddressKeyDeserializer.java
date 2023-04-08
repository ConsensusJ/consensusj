package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.DefaultAddressParser;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;

/**
 * Key Deserializer to support using Address as the key in a Map.
 */
public class AddressKeyDeserializer extends KeyDeserializer {
    private final AddressParser.Strict addressParser;

    /**
     * Construct an address deserializer that will deserialize addresses for any supported network.
     * See {@link NetworkParameters} to understand what the supported networks are.
     */
    public AddressKeyDeserializer() {
        addressParser = (s) -> new DefaultAddressParser().parseAddressAnyNetwork(s);
    }

    /**
     * Construct an address KeyDeserializer that validates addresses for the specified {@link NetworkParameters}.
     * When deserializing addresses, addresses that are not from the specified network will cause a
     * {@link InvalidFormatException} to be thrown during deserialization.
     *
     * @param network specify the only network we will deserialize addresses for.
     */
    public AddressKeyDeserializer(Network network) {
        addressParser = (network != null)
                ? (s) -> new DefaultAddressParser().parseAddress(s, network)
                : (s) -> new DefaultAddressParser().parseAddressAnyNetwork(s);
    }

    /**
     * @deprecated use {@link #AddressKeyDeserializer(Network)}
     */
    public AddressKeyDeserializer(NetworkParameters netParams) {
        this(netParams.network());
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return addressParser.parseAddress(key);
    }
}
