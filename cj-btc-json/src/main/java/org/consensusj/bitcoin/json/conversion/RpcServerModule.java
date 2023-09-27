package org.consensusj.bitcoin.json.conversion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.Peer;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;

/**
 * Jackson Module with serializers and deserializers for JSON-RPC servers.
 */
public class RpcServerModule extends SimpleModule {
    public RpcServerModule() {
        super("BitcoinJMappingServer", new Version(1, 0, 0, null, null, null));

        this.addDeserializer(Address.class, new AddressDeserializer())
                .addDeserializer(Coin.class, new CoinDeserializer())
                .addDeserializer(ECKey.class, new ECKeyDeserializer())
                .addDeserializer(Sha256Hash.class, new Sha256HashDeserializer())
                .addSerializer(Address.class, new AddressSerializer())
                .addSerializer(Coin.class, new CoinSerializer())
                .addSerializer(ECKey.class, new ECKeySerializer())
                .addSerializer(Peer.class, new PeerSerializer())
                .addSerializer(Sha256Hash.class, new Sha256HashSerializer())
                .addSerializer(Transaction.class, new TransactionSerializer())
                .addSerializer(Script.class, new ScriptSerializer());
    }
}
