package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Address;

import java.io.IOException;

/**
 *
 */
public class AddressSerializer extends JsonSerializer<Address> {
    @Override
    public void serialize(Address value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(value.toString());
    }
}
