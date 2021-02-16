package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Transaction;

import java.io.IOException;

/**
 * Custom Serialization of Transaction Peer class
 */
public class TransactionSerializer extends JsonSerializer<Transaction> {
    @Override
    public void serialize(Transaction value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        // See RawTransactionInfo POJO for a more complete JSON representation of a Transaction that is used by JSON-RPC
        jgen.writeNumberField("confirmations", value.getConfidence().getDepthInBlocks());
        jgen.writeNumberField("version", value.getVersion());
        jgen.writeStringField("hash", value.getTxId().toString());
        jgen.writeEndObject();
    }
}
