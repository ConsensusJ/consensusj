package com.msgilligan.bitcoinj.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Peer;

import java.io.IOException;

/**
 * Custom Serialization of bitcoinj Peer class
 */
public class PeerSerializer extends JsonSerializer<Peer> {
    @Override
    public void serialize(Peer value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("socketAddress", value.getAddress().toSocketAddress().toString());
        jgen.writeNumberField("remoteVersion", value.getPeerVersionMessage().clientVersion);
        jgen.writeNumberField("bestHeight", value.getPeerVersionMessage().bestHeight);
        jgen.writeEndObject();
    }
}
