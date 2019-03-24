package com.msgilligan.bitcoinj.spring.config;

import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import org.bitcoinj.core.PeerGroup;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.params.MainNetParams;
import com.msgilligan.bitcoinj.spring.services.PeerStompService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.Module;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Spring configuration for bitcoinj, Bitcoin services, and JSON-RPC server
 */
@Configuration
public class BitcoinConfig {
    private static final Logger log = LoggerFactory.getLogger(BitcoinConfig.class);

    @Bean
    public NetworkParameters networkParameters() {
        return MainNetParams.get();
    }

    @Bean
    public PeerDiscovery peerDiscovery(NetworkParameters params) throws FileNotFoundException {
        PeerDiscovery pd;
        pd = new DnsDiscovery(params);
//        pd = new SeedPeers(params);
        return pd;
    }

    @Bean
    PeerGroup peerGroup(Context context, PeerDiscovery peerDiscovery) {
        PeerGroup peerGroup = new PeerGroup(context);
        peerGroup.addPeerDiscovery(peerDiscovery);
        peerGroup.start();
        return peerGroup;
    }

    @Bean
    public Context getContext(NetworkParameters params) {
        log.info("Creating context with {}", params.getId());
        Context context = new Context(params);
        log.info("Context is {}", context.getParams().getId());
        return context;
    }
    
    @Bean
    public WalletAppKit getKit(NetworkParameters params) throws Exception {
        // TODO: make File(".") and filePrefix configurable
        File directory = new File(".");
        String filePrefix = "ConensusJ-BTC-Daemon";

        return new WalletAppKit(params, directory, filePrefix);
    }

    @Bean
    public Module bitcoinJMapper(NetworkParameters params) {
        return new RpcServerModule(params);
    }
    
    @Bean
    public WalletAppKitService walletAppKitService(NetworkParameters params, Context context, WalletAppKit kit) {
        log.info("WalletAppKitService factory params are {}", params.getId());
        log.info("WalletAppKitService factory context is {}", context.getParams().getId());
        return new WalletAppKitService(params, context, kit);
    }

    @Bean
    PeerStompService peerStompService(Context context,
                                      PeerGroup peerGroup,
                                      SimpMessageSendingOperations messagingTemplate) {
        return new PeerStompService(context, peerGroup, messagingTemplate);
    }

    @Bean(name="/jsonrpc")
    public JsonServiceExporter bitcoinServiceExporter(WalletAppKitService walletAppKitService) {
        JsonServiceExporter exporter = new JsonServiceExporter();
        exporter.setService(walletAppKitService);
        exporter.setServiceInterface(BitcoinJsonRpc.class);
        exporter.setBackwardsComaptible(true);
        return exporter;
    }
}
