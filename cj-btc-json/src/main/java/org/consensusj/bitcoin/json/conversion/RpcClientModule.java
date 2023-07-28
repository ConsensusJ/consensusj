package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.Block;
import org.bitcoinj.base.Coin;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;

import java.util.Objects;

// TODO: Consider merging RpcClientModule and RpcServerModule (though there are currently conflicting serializers for Transaction)
/**
 * Jackson Module with serializers and deserializers for JSON-RPC clients.
 */
public class RpcClientModule extends SimpleModule {
    /**
     * Create a client module without strict address parsing.
     */
    public RpcClientModule() {
        this(null, false);
    }

    /**
     * @param network Which network we are going to be a client for (may be null if {@code strictAddressParsing} is false)
     * @param strictAddressParsing set to {@code true} to throw exceptions when deserializing addresses that don't match {@code network}
     */
    protected RpcClientModule(Network network, boolean strictAddressParsing) {
        super("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null));

        this.addDeserializer(Address.class, strictAddressParsing ? new AddressDeserializer(Objects.requireNonNull(network)) : new AddressDeserializer())
                .addDeserializer(Block.class, new BlockHexDeserializer())
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
     * Create a client module with strict address parsing. Will throw exceptions when deserializing addresses that don't match {@code network}.
     * @param network Expected Network we are going to validate addresses for
     */
    public RpcClientModule(Network network) {
        this(network, true);
    }
}
