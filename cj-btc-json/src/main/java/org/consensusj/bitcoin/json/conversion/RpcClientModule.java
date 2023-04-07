package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bitcoinj.base.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Coin;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;

/**
 *
 */
public class RpcClientModule extends SimpleModule {
    /**
     * @param netParams Which network we are going to be a client for
     * @param strictAddressParsing set to {@code false} to not throw exception on addresses from other networks
     */
    public RpcClientModule(NetworkParameters netParams, boolean strictAddressParsing) {
        super("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null));

        this.addDeserializer(Address.class, new AddressDeserializer(strictAddressParsing ? netParams : null))
                .addDeserializer(Block.class, new BlockHexDeserializer(netParams))
                .addDeserializer(Coin.class, new CoinDeserializer())
                .addDeserializer(ECKey.class, new ECKeyDeserializer())
                .addDeserializer(Sha256Hash.class, new Sha256HashDeserializer())
                .addSerializer(Address.class, new AddressSerializer())
                .addSerializer(Coin.class, new CoinSerializer())
                .addSerializer(ECKey.class, new ECKeySerializer())
                .addSerializer(Sha256Hash.class, new Sha256HashSerializer())
                .addSerializer(Transaction.class, new TransactionHexSerializer());
    }

    /**
     * @param netParams Which network we are going to be a client for
     */
    public RpcClientModule(NetworkParameters netParams) {
        this(netParams, true);
    }
}
