package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.base.Address;
import org.bitcoinj.script.Script;

import java.io.IOException;

/**
 *
 */
public class ScriptSerializer extends JsonSerializer<Script>  {
    @Override
    public void serialize(Script script, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(HexUtil.bytesToHexString(script.program()));
    }
}
