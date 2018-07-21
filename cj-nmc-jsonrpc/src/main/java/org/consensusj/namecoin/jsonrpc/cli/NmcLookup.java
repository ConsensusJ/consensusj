package org.consensusj.namecoin.jsonrpc.cli;

import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.consensusj.namecoin.jsonrpc.pojo.NameData;
import org.consensusj.namecoin.jsonrpc.NamecoinClient;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Simple command-line tool to do Namecoin name lookup
 */
public class NmcLookup {

    public static void main(String[] args) throws URISyntaxException, IOException, JsonRpcStatusException, AddressFormatException {
        NamecoinClient client = new NamecoinClient(NamecoinClient.readConfig());
        NameData result = client.nameShow("d/beelin");
        System.out.println(result.getValue());
        Address owningAddress = result.getAddress();
        System.out.println("Owning Address: " + owningAddress);
    }
}
