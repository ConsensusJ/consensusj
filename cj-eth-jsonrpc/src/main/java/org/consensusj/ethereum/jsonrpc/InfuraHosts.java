package org.consensusj.ethereum.jsonrpc;

import java.net.URI;

/**
 * Container for Infura host URIs and API Key
 */
class InfuraHosts {
    public static String INFURA_API_KEY = getInfuraApiKey();
    public static URI INFURA_MAINNET_HOST = URI.create("https://mainnet.infura.io/" + INFURA_API_KEY);
    public static URI INFURA_ROPSTEN_HOST = URI.create("https://ropsten.infura.io/" + INFURA_API_KEY);
    public static URI INFURA_RINKEBYHOST = URI.create("https://rinkeby.infura.io/" + INFURA_API_KEY);

    static String getInfuraApiKey() {
        String env = System.getenv("INFURA_API_KEY");
        String prop = System.getProperty("org.consensusj.ethereum.infuraApiKey", "");
        return (env != null) ? env : prop;
    }
}
