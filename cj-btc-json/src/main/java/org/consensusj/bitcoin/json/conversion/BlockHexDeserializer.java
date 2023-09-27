package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.ProtocolException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Deserializes a hex string as a Bitcoin {@link Block}
 */
public class BlockHexDeserializer extends JsonDeserializer<Block> {

    public BlockHexDeserializer() {
    }
    @Override
    public Block deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    byte[] payload = HexUtil.hexStringToByteArray(p.getValueAsString()); // convert  to hex
                    return Block.read(ByteBuffer.wrap(payload));
                } catch (ProtocolException e) {
                    throw new InvalidFormatException(p, "Invalid Block", p.getValueAsString(), Block.class);
                }
            default:
                return (Block) ctxt.handleUnexpectedToken(Block.class, p);
        }
    }
}
