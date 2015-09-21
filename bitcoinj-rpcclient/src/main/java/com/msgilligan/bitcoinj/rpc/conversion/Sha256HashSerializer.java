package com.msgilligan.bitcoinj.rpc.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;

/**
 *
 */
public class Sha256HashSerializer extends JsonSerializer<Sha256Hash> {
    @Override
    public void serialize(Sha256Hash value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(value.toString());
    }
}
