package org.consensusj.bitcoin.cli;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile;
import org.apache.commons.cli.Options;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.consensusj.jsonrpc.JsonRpcException;
import org.consensusj.jsonrpc.cli.BaseJsonRpcTool;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * An attempt at cloning the bitcoin-cli tool, but using Java and bitcoinj
 *
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
     * for the methods and parameters. Users can use `-?` to get general help or `help <command>` to get help
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
        return new BitcoinCLICall(out, err, args);
    }

    /**
     * Convert params from strings to Java types that will map to correct JSON types
     *
     * TODO: Make this better and complete
     *
     * @param method the JSON-RPC method
     * @param params Params with String type
     * @return Params with correct Java types for JSON
     */
    protected List<Object> convertParameters(String method, List<String> params) {
        List<Object> typedParams = new ArrayList<>();
        switch (method) {
            case "generate":
            case "setgenerate":
                typedParams.add(Integer.valueOf(params.get(0)));
                break;

            case "getblockhash":
                typedParams.add(Integer.valueOf(params.get(0)));
                break;
                
            default:
                // Default (for now) is to leave them all as strings
                for (String string : params) {
                    typedParams.add(string);
                }

        }
        return typedParams;
    }

    public class BitcoinCLICall extends BaseJsonRpcTool.CommonsCLICall {
        private BitcoinClient client;
        private final boolean rpcWait;

        public BitcoinCLICall(PrintWriter out, PrintWriter err, String[] args) {
            super(out, err, args);
                rpcWait = line.hasOption("rpcwait");
        }

        @Override
        public BitcoinClient rpcClient() {
            if (client == null) {
                System.out.println("Connecting to: " + getRPCConfig().getURI());
                RpcConfig config = getRPCConfig();
                client = new BitcoinClient( config.getNetParams(),
                        config.getURI(),
                        config.getUsername(),
                        config.getPassword());
                if (rpcWait) {
                    boolean available = false;   // Wait up to 1 hour
                    try {
                        available = client.waitForServer(60*60);
                    } catch (JsonRpcException e) {
                        printError(this, "JSON-RPC Exception: " + e.getMessage());
                        throw new ToolException(1, e.getMessage());
                    }
                    if (!available) {
                        printError(this,"Timeout error.");
                        throw new ToolException(1, "Timeout error.");
                    }
                }
            }
            return client;
        }

        private RpcConfig getRPCConfig() {
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


    }
}
