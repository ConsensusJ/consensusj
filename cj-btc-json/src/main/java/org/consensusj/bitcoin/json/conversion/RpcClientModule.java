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

// TODO: In (planned) bitcoinj 0.17-alpha2 Network/NetworkParameters is no longer needed to construct this class
// TODO: After bitcoinj 0.17-alpha2 maybe merge RpcClientModule and RpcServerModule (though there are currently conflicting serializers for Transaction)
/**
 * Jackson Module with serializers and deserializers for JSON-RPC clients.
 */
public class RpcClientModule extends SimpleModule {
    /**
     * @param network Which network we are going to be a client for
     * @param strictAddressParsing set to {@code false} to not throw exception on addresses from other networks
     */
    public RpcClientModule(Network network, boolean strictAddressParsing) {
        super("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null));

        this.addDeserializer(Address.class, strictAddressParsing ? new AddressDeserializer(network) : new AddressDeserializer())
                .addDeserializer(Block.class, new BlockHexDeserializer(network))
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
     * @param network Which network we are going to be a client for
     */
    public RpcClientModule(Network network) {
        this(network, true);
    }
}
