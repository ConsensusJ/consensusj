package com.msgilligan.bitcoinj.rpc.conversion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

/**
 *
 */
public class RpcClientModule extends SimpleModule {
    public RpcClientModule() {
        super("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null));
        this.addDeserializer(Address.class, new AddressDeserializer())
            .addDeserializer(Coin.class, new CoinDeserializer())
            .addDeserializer(ECKey.class, new ECKeyDeserializer())
            .addDeserializer(Sha256Hash.class, new Sha256HashDeserializer())
            .addSerializer(Address.class, new AddressSerializer())
            .addSerializer(Coin.class, new CoinSerializer())
            .addSerializer(Sha256Hash.class, new Sha256HashSerializer())
            .addSerializer(Transaction.class, new TransactionSerializer());
    }
}
