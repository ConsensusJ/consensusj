package org.consensusj.bitcoin.cli;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.cli.CliCommand;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public abstract class BitcoinCliCommand extends CliCommand {
    protected BitcoinClient client = null;

    protected BitcoinCliCommand(String name, CliOptions options, String[] args) {
        this(name, name, options, args);
    }

    protected BitcoinCliCommand(String name, String usage, CliOptions options, String[] args) {
        super(name, usage, options, args);
    }

    public BitcoinClient getClient() {
        if (client == null) {
            System.out.println("Connecting to: " + getRPCConfig().getURI());
            RpcConfig config = getRPCConfig();
            client = new BitcoinClient( config.getNetParams(),
                    config.getURI(),
                    config.getUsername(),
                    config.getPassword());
        }
        return client;
    }

    /**
     * Initial a client and if rpcwait option set, make sure server is accepting connections.
     * @return status code
     */
    public int preflight() {
        getClient();
        if (line.hasOption("rpcwait")) {
            boolean available = false;   // Wait up to 1 hour
            try {
                available = client.waitForServer(60*60);
            } catch (JsonRpcException e) {
                this.pwerr.println("JSON-RPC Exception: " + e.getMessage());
                return 1;
            }
            if (!available) {
                this.pwerr.println("Timeout error.");
                return 1;
            }
        }
        return 0;
    }

    protected URI getServerURI(URI confFileURI) {
        String scheme = confFileURI.getScheme();
        String host = confFileURI.getHost();
        int port = confFileURI.getPort();
        String file = confFileURI.getPath();

        if (line.hasOption("rpcssl")) {
            scheme = "https";
        }
        if (line.hasOption("rpcconnect")) {
            host = line.getOptionValue("rpcconnect", host);
        }
        if (line.hasOption("regtest") || line.hasOption("testnet")) {
            port = 18332;
        }
        if (line.hasOption("rpcport")) {
            String portString = line.getOptionValue("rpcport");
            port = Integer.parseInt(portString);
        }
        URI rpcServerURI = null;
        try {
            rpcServerURI = new URI(scheme, null, host, port, file, null, null);
        } catch (URISyntaxException e) {
            // We should be careful that this never happens
            e.printStackTrace();
            // But if it does, throw an unchecked exception
            throw new RuntimeException(e);
        }
        return rpcServerURI;
    }

    protected RpcConfig getRPCConfig() {
        RpcConfig confFileConfig = BitcoinConfFile.readDefaultConfig().getRPCConfig();
        URI uri = getServerURI(confFileConfig.getURI());
        String user = line.getOptionValue("rpcuser", confFileConfig.getUsername());
        String pass = line.getOptionValue("rpcpassword", confFileConfig.getPassword());
        NetworkParameters netParams;
        if (line.hasOption("regtest")) {
            netParams = RegTestParams.get();
        } else if (line.hasOption(("testnet"))) {
            netParams = TestNet3Params.get();
        } else {
            netParams = MainNetParams.get();
        }
        RpcConfig cfg = new RpcConfig(netParams, uri, user, pass);
        return cfg;
    }

}
