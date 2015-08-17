package com.msgilligan.bitcoinj.spring.config;

import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpcImpl;
import com.msgilligan.bitcoinj.jackson.SerializerModule;
import com.msgilligan.bitcoinj.spring.service.PeerService;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.Module;

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
        return new SerializerModule();
    }

//    @Bean
//    public AutoJsonRpcServiceExporter jsonRpcServiceExporter() {
//        return new AutoJsonRpcServiceExporter();
//    }

    @Bean
    public BitcoinJsonRpc bitcoinRPCService(PeerService peerService) {
        return new BitcoinJsonRpcImpl(peerService);
    }

    @Bean(name="/bitcoinrpc")
    public JsonServiceExporter bitcoinServiceExporter(BitcoinJsonRpc bitcoinJsonRPCService) {
        JsonServiceExporter exporter = new JsonServiceExporter();
        exporter.setService(bitcoinJsonRPCService);
        exporter.setServiceInterface(BitcoinJsonRpc.class);
        return exporter;
    }
}
