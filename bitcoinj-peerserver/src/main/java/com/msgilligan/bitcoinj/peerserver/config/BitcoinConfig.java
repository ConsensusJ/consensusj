package com.msgilligan.bitcoinj.peerserver.config;

import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import com.msgilligan.bitcoinj.spring.service.PeerService;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.Module;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.FileNotFoundException;

/**
 */
@Configuration
public class BitcoinConfig {
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
    public Module bitcoinJMapper() {
        return new RpcServerModule();
    }

    @Bean
    public PeerService peerGroupService(NetworkParameters params, PeerDiscovery peerDiscovery, SimpMessageSendingOperations simpMessageSendingOperations) {
        return new PeerService(params, peerDiscovery, simpMessageSendingOperations);
    }


    @Bean(name="/bitcoinrpc")
    public JsonServiceExporter bitcoinServiceExporter(PeerService peerService) {
        JsonServiceExporter exporter = new JsonServiceExporter();
        exporter.setService(peerService);
        exporter.setServiceInterface(BitcoinJsonRpc.class);
        return exporter;
    }
}
