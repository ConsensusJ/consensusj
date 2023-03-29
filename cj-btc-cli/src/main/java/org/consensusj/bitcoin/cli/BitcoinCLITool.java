package org.consensusj.bitcoin.cli;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile;
import org.apache.commons.cli.Options;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.cli.BaseJsonRpcTool;

import javax.net.ssl.SSLSocketFactory;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static org.consensusj.bitcoin.jsonrpc.RpcURI.RPCPORT_REGTEST;
import static org.consensusj.bitcoin.jsonrpc.RpcURI.RPCPORT_TESTNET;

/**
 * An attempt at cloning the bitcoin-cli tool, but using Java and bitcoinj
 */
public class BitcoinCLITool extends BaseJsonRpcTool {
    public final static String commandName = "cj-bitcoin-cli";
    private final Options options;

    public BitcoinCLITool() {
        this.options = new BitcoinRpcCliOptions();
    }

    @Override
    public String name() {
        return commandName;
    }

    @Override
    public Options options() {
        return options;
    }

    /**
     * main method for bitcoinj-cli tool.
     *
     * See {@link BitcoinRpcCliOptions} for options and https://bitcoin.org/en/developer-reference#bitcoin-core-apis[Bitcoin Core JSON-RPC API]
     * for the methods and parameters. Users can use `-?` to get general help or {@code help _command_} to get help
     * on a specific command.
     *
     * @param args options, JSON-RPC method, JSON-RPC parameters
     */
    public static void main(String[] args) {
        BitcoinCLITool command = new BitcoinCLITool();
        int status = command.run(System.out, System.err, args);
        System.exit(status);
    }

    @Override
    public BitcoinCLICall createCall(PrintWriter out, PrintWriter err, String... args) {
        return new BitcoinCLICall(this, out, err, args);
    }

    public static class BitcoinCLICall extends BaseJsonRpcTool.CommonsCLICall {
        private BitcoinClient client;
        private final boolean rpcWait;

        public BitcoinCLICall(BitcoinCLITool tool, PrintWriter out, PrintWriter err, String[] args) {
            super(tool, out, err, args);
            rpcWait = line.hasOption("rpcwait");
        }

        @Override
        public BitcoinClient rpcClient(SSLSocketFactory sslSocketFactory) {
            // Not threadsafe
            // This needs work if there are ever going to be multiple clients calling this method
            if (client == null) {
                RpcConfig config = getRPCConfig();
                client = createClient(sslSocketFactory, config);
                if (rpcWait) {
                    // TODO: Add logging here to replace the System.out.println
                    //System.out.println("Connecting to: " + getRPCConfig().getURI() + " with -rpcWait");
                    boolean available = false;   // Wait up to 1 hour
                    try {
                        available = client.waitForServer(60*60);
                    } catch (JsonRpcException e) {
                        rpcTool.printError(this, "JSON-RPC Exception: " + e.getMessage());
                        throw new ToolException(1, e.getMessage());
                    }
                    if (!available) {
                        rpcTool.printError(this,"Timeout error.");
                        throw new ToolException(1, "Timeout error.");
                    }
                }
            }
            return client;
        }

        @Override
        public BitcoinClient rpcClient() {
            return rpcClient((SSLSocketFactory) SSLSocketFactory.getDefault());
        }

        /**
         * Override this method to customize the client implementation subclass
         * @param config Configuration information for connecting to server
         * @return a newly constructed BitcoinClient with specified config
         */
        protected BitcoinClient createClient(SSLSocketFactory sslSocketFactory, RpcConfig config) {
            return new BitcoinClient( sslSocketFactory, config.network(),
                    config.getURI(),
                    config.getUsername(),
                    config.getPassword());
        }

        public RpcConfig getRPCConfig() {
            RpcConfig confFileConfig = BitcoinConfFile.readDefaultConfig().getRPCConfig();
            URI uri = getServerURI(confFileConfig.getURI());
            String user = line.getOptionValue("rpcuser", confFileConfig.getUsername());
            String pass = line.getOptionValue("rpcpassword", confFileConfig.getPassword());
            Network network;
            if (line.hasOption("regtest")) {
                network = BitcoinNetwork.REGTEST;
            } else if (line.hasOption(("testnet"))) {
                network = BitcoinNetwork.TESTNET;
            } else {
                // TODO: Use network params from BitcoinConfFile, before falling back
                network =BitcoinNetwork.MAINNET;
            }
            return new RpcConfig(network, uri, user, pass);
        }

        private URI getServerURI(URI confFileURI) {
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
            if (line.hasOption("regtest"))  {
                port = RPCPORT_REGTEST;
            } else if (line.hasOption("testnet")) {
                port = RPCPORT_TESTNET;
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


    }
}
