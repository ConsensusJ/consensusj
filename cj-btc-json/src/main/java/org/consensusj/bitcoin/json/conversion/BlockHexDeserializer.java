package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;

import java.io.IOException;

/**
 *
 */
public class BlockHexDeserializer extends JsonDeserializer<Block> {
    private final Context context;

    public BlockHexDeserializer(NetworkParameters netParams) {
        this.context = new Context(netParams);
    }

    @Override
    public Block deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    byte[] payload = HexUtil.hexStringToByteArray(p.getValueAsString()); // convert  to hex
                    return context.getParams().getDefaultSerializer().makeBlock(payload);
                } catch (ProtocolException e) {
                    throw new InvalidFormatException(p, "Invalid Block", p.getValueAsString(), Block.class);
                }
            default:
                return (Block) ctxt.handleUnexpectedToken(Block.class, p);
        }
    }
}
