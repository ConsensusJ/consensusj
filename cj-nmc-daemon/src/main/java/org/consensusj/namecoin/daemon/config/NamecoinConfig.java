package org.consensusj.namecoin.daemon.config;

import com.fasterxml.jackson.databind.Module;
import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import com.msgilligan.bitcoinj.spring.service.PeerGroupService;
import com.msgilligan.bitcoinj.spring.service.WalletAppKitService;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.kits.WalletAppKit;
import org.libdohj.params.NamecoinMainNetParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Spring configuration for namecoinj, Namecoin services, and JSON-RPC server
 */
@Configuration
public class NamecoinConfig {
    private Context context;
    private WalletAppKit kit;

    @Bean
    public NetworkParameters networkParameters() {
        // TODO: We may also want to make this set from a configuration string
        // so a binary release can be configure via external string parameters
        // and NetworkParameters.fromID()
        return NamecoinMainNetParams.get();
    }

    @Bean
    public PeerDiscovery peerDiscovery(NetworkParameters params) throws FileNotFoundException {
        PeerDiscovery pd;
        pd = new DnsDiscovery(params);
//        pd = new SeedPeers(params);
        return pd;
    }

    @Bean
    public Context getContext(NetworkParameters params) {
        if (context == null) {
            context = new Context(params);
        }
        return context;
    }

    @Bean
    public WalletAppKit getKit(Context context) throws Exception {
        if (kit == null) {

            // TODO: make File(".") and filePrefix configurable
            File directory = new File(".");
            String filePrefix = "NamecoinJDaemon";

            kit = new WalletAppKit(context, directory, filePrefix);
        }

        return kit;
     }

    @Bean
    public Module bitcoinJMapper() {
        return new RpcServerModule();
    }

    @Bean
    public PeerGroupService peerGroupService(NetworkParameters params, PeerDiscovery peerDiscovery) {
        return new PeerGroupService(params, peerDiscovery);
    }

    @Bean
    public WalletAppKitService walletAppKitService(NetworkParameters params, Context context, WalletAppKit kit) {
        return new WalletAppKitService(params, context, kit);
    }

    @Bean(name="/")
    public JsonServiceExporter bitcoinServiceExporter(WalletAppKitService walletAppKitService) {
        JsonServiceExporter exporter = new JsonServiceExporter();
        exporter.setService(walletAppKitService);
        exporter.setServiceInterface(BitcoinJsonRpc.class);
        exporter.setBackwardsComaptible(true);
        return exporter;
    }
}
