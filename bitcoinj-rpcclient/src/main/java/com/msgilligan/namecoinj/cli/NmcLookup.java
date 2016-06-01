package com.msgilligan.namecoinj.cli;

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import com.msgilligan.bitcoinj.rpc.bitcoind.AppDataDirectory;
import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile;
import com.msgilligan.namecoinj.core.NMCMainNetParams;
import com.msgilligan.namecoinj.json.pojo.NameData;
import com.msgilligan.namecoinj.rpc.NamecoinClient;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simple command-line tool to do Namecoin name lookup
 */
public class NmcLookup {

    public static void main(String[] args) throws URISyntaxException, IOException, JsonRPCStatusException, AddressFormatException {
        RPCConfig config = readConfig();
        NetworkParameters netParams = NMCMainNetParams.get();
        NamecoinClient client = new NamecoinClient(netParams,
                config.getURI(), config.getUsername(), config.getPassword());
        NameData result = client.nameShow("d/beelin");
        System.out.println(result.getValue());
        Address owningAddress = result.getAddress();
        System.out.println("Owning Address: " + owningAddress);
    }

    /**
     * Namecoin conf file uses same format as bitcoin.conf
     * @return object containing RPC endpoint information
     */
    static RPCConfig readConfig() {
        File file = new File(AppDataDirectory.forAppName("Namecoin"), "namecoin.conf");
        BitcoinConfFile conf = new BitcoinConfFile(file);
        return conf.read().getRPCConfig();
    }
}
