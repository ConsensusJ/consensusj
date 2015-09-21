package com.msgilligan.bitcoinj.rpc.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bitcoinj.core.Coin;

import java.io.IOException;

/**
 *
 */
public class CoinSerializer  extends JsonSerializer<Coin> {
    @Override
    public void serialize(Coin value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeNumber(BitcoinMath.coinToBTC(value));
    }
}
