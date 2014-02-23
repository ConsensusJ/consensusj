package com.msgilligan.peerlist.config;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.google.bitcoin.net.discovery.SeedPeers;
import com.google.bitcoin.params.MainNetParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;

/**
 * User: sean
 * Date: 2/22/14
 * Time: 7:56 PM
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
//        pd = new DnsDiscovery(params);
        pd = new SeedPeers(params);
        return pd;
    }
}
