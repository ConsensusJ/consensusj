package com.msgilligan.ethereum.rpc

/**
 * Container for Infura host URIs and API Key
 */
class InfuraHosts {
    public static String INFURA_API_KEY
    public static URI INFURA_MAINNET_HOST
    public static URI INFURA_ROPSTEN_HOST

    static {
        INFURA_API_KEY = "goes-here";
        try {
            INFURA_MAINNET_HOST = new URI("https://mainnet.infura.io/" + INFURA_API_KEY);
            INFURA_ROPSTEN_HOST = new URI("https://ropsten.infura.io/" + INFURA_API_KEY);
        } catch (URISyntaxException ignored) {
            INFURA_MAINNET_HOST = null;
            INFURA_ROPSTEN_HOST = null;
        }
    }
}
