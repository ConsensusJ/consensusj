package com.msgilligan.namecoinj.cli;

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException;
import com.msgilligan.namecoinj.core.NMCMainNetParams;
import com.msgilligan.namecoinj.json.pojo.NameData;
import com.msgilligan.namecoinj.rpc.NamecoinClient;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simple command-line tool to do Namecoin name lookup
 */
public class NmcLookup {
    public static String rpcName = "xxx";
    public static String rpcPassword = "yyy";

    public static void main(String[] args) throws URISyntaxException, IOException, JsonRPCStatusException, AddressFormatException {
        URI server = new URI("http://localhost:8336");
        NetworkParameters netParams = NMCMainNetParams.get();
        NamecoinClient client = new NamecoinClient(netParams, server, rpcName, rpcPassword);
        NameData result = client.nameShow("d/beelin");
        System.out.println(result.getValue());
        Address owningAddress = result.getAddress();
        System.out.println("Owning Address: " + owningAddress);
    }
}
