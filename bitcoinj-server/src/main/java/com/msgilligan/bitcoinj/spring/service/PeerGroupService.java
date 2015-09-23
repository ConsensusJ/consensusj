package com.msgilligan.bitcoinj.spring.service;

import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.PeerDiscovery;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implement a subset of Bitcoin JSON RPC using only a PeerGroup
 */
@Named
public class PeerGroupService implements BitcoinJsonRpc {
    private static final String userAgentName = "PeerList";
    private static final String appVersion = "0.1";
    protected NetworkParameters netParams;
    protected PeerGroup peerGroup;

    @Inject
    public PeerGroupService(NetworkParameters params,
                       PeerDiscovery peerDiscovery) {
        this.netParams = params;
        this.peerGroup = new PeerGroup(params);
        peerGroup.setUserAgent(userAgentName, appVersion);
        peerGroup.addPeerDiscovery(peerDiscovery);
    }

    @PostConstruct
    public void start() {
        peerGroup.startAsync();
    }

    public NetworkParameters getNetworkParameters() {
        return this.netParams;
    }

    @Override
    public Integer getblockcount() {
        return peerGroup.getMostCommonChainHeight();
    }

    @Override
    public Integer getconnectioncount() {
        return peerGroup.numConnectedPeers();
    }

}
