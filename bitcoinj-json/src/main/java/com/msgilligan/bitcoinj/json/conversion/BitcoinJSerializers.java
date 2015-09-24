package com.msgilligan.bitcoinj.json.conversion;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;

/**
 *
 */
@Deprecated
public class BitcoinJSerializers extends Serializers.Base {
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> clazz = type.getRawClass();

        if (Peer.class.isAssignableFrom(clazz)) {
            return new PeerSerializer();
        }
        if (Transaction.class.isAssignableFrom(clazz)) {
            return new TransactionSerializer();
        }
        return super.findSerializer(config, type, beanDesc);
    }
}

