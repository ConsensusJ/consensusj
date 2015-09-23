package com.msgilligan.bitcoinj.rpc.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;

import java.io.IOException;

/**
 *
 */
public class BlockDeserializer  extends JsonDeserializer<Block> {
    private final Context context;

    public BlockDeserializer(NetworkParameters netParams) {
        this.context = new Context(netParams);
    }

    @Override
    public Block deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    byte[] payload = BitcoinClient.hexStringToByteArray(p.getValueAsString()); // convert  to hex
                    return new Block(context.getParams(), payload, true, false, payload.length);
                } catch (ProtocolException e) {
                    throw new InvalidFormatException("Invalid Block", p.getValueAsString(), Block.class);
                }
            default:
                throw ctxt.mappingException(Block.class, token);
        }
    }
}
