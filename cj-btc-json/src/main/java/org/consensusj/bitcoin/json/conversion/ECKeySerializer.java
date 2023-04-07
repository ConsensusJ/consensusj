package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.crypto.ECKey;

import java.io.IOException;

/**
 * Serialize (without private key)
 */
public class ECKeySerializer extends JsonSerializer<ECKey> {
    @Override
    public void serialize(ECKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(value.getPublicKeyAsHex());
    }
}
