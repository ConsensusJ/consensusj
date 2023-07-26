package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;

import java.io.IOException;
import java.nio.ByteBuffer;

// TODO: In (planned) bitcoinj 0.17-alpha2 Network/NetworkParameters is no longer needed to construct blocks
/**
 * Deserializes a hex string as a Bitcoin {@link Block}
 */
public class BlockHexDeserializer extends JsonDeserializer<Block> {
    private final NetworkParameters netParams;

    public BlockHexDeserializer(Network network) {
        this.netParams = NetworkParameters.of(network);
    }

    @Deprecated
    public BlockHexDeserializer(NetworkParameters netParams) {
        this.netParams = netParams;
    }

    @Override
    public Block deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                try {
                    byte[] payload = HexUtil.hexStringToByteArray(p.getValueAsString()); // convert  to hex
                    // TODO: return Block.read(ByteBuffer.wrap(payload));
                    return netParams.getDefaultSerializer().makeBlock(ByteBuffer.wrap(payload));
                } catch (ProtocolException e) {
                    throw new InvalidFormatException(p, "Invalid Block", p.getValueAsString(), Block.class);
                }
            default:
                return (Block) ctxt.handleUnexpectedToken(Block.class, p);
        }
    }
}
