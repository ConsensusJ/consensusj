package com.msgilligan.namecoinj.rpc;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException;
import com.msgilligan.namecoinj.json.pojo.NameData;
import org.bitcoinj.core.NetworkParameters;

import java.io.IOException;
import java.net.URI;

/**
 * Namecoin RPC client
 * See: https://wiki.namecoin.org/index.php?title=Client_API
 */
public class NamecoinClient extends BitcoinClient {
    public NamecoinClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword);
    }

    /**
     * name_show
     *
     * @param identifier namespace/name, e.g. 'd/beelin'
     * @return Data object
     * @throws IOException
     * @throws JsonRPCStatusException
     */
    public NameData nameShow(String identifier) throws IOException, JsonRPCStatusException {
        return send("name_show", NameData.class, identifier);
    }
}
