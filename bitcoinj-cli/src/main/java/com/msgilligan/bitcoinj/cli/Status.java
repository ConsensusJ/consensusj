package com.msgilligan.bitcoinj.cli;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.json.pojo.NetworkInfo;
import com.msgilligan.jsonrpc.JsonRPCException;

import java.io.IOException;

/**
 * A command-line client that prints some basic information retrieved via RPC
 */
public class Status extends CliCommand {
    public final static String commandName = "btcstatus";

    public Status(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    public static void main(String[] args) {
        Status command = new Status(args);
        Integer status = command.run();
        System.exit(status);
    }

    @Override
    public Integer runImpl() throws IOException {
        NetworkInfo networkInfo;
        BlockChainInfo chainInfo;
        try {
            networkInfo = client.getNetworkInfo();
            chainInfo = client.getBlockChainInfo();
        } catch (JsonRPCException e) {
            e.printStackTrace();
            return 1;
        }

        Integer bitcoinVersion = networkInfo.getVersion();
        Integer blocks = chainInfo.getBlocks();

        pwout.println("Bitcoin Core Version: " + bitcoinVersion);
        pwout.println("Block count: " + blocks);
        return 0;
    }
}
