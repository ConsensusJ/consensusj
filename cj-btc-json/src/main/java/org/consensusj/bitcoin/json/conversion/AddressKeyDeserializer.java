package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.exceptions.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;

/**
 * Key Deserializer to support using Address as the key in a Map.
 */
public class AddressKeyDeserializer extends KeyDeserializer {
    private final AddressParser addressParser;

    /**
     * Construct an address deserializer that will deserialize addresses for the default supported networks.
     * See {@link Network} to understand what the supported networks are.
     */
    public AddressKeyDeserializer() {
        this(AddressParser.getDefault());
    }

    /**
     * Construct an address KeyDeserializer with a custom {@link AddressParser}
     * @param addressParser parser to convert a string to an address
     */
    public AddressKeyDeserializer(AddressParser addressParser) {
        this.addressParser = addressParser;
    }

    /**
     * Construct an address KeyDeserializer that validates addresses for the specified {@link Network}.
     * When deserializing addresses, addresses that are not from the specified network will cause a
     * {@link InvalidFormatException} to be thrown during deserialization.
     *
     * @param network specify the only network we will deserialize addresses for.
     */
    public AddressKeyDeserializer(Network network) {
        this( (network != null)
                ? AddressParser.getDefault(network)
                : AddressParser.getDefault());
    }

    /**
     * @deprecated use {@link #AddressKeyDeserializer(Network)}
     */
    @Deprecated
    public AddressKeyDeserializer(NetworkParameters netParams) {
        this(netParams.network());
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        try {
            return addressParser.parseAddress(key);
        } catch (AddressFormatException afe) {
            throw new InvalidFormatException(ctxt.getParser(), "Invalid Address: " + key, key, Address.class);
        }
    }
}
