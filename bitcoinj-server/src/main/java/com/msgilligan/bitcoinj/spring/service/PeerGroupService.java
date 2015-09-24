package com.msgilligan.bitcoinj.spring.service;

import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.net.discovery.PeerDiscovery;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;

/**
 * Implement a subset of Bitcoin JSON RPC using only a PeerGroup
 */
@Named
public class PeerGroupService implements BitcoinJsonRpc {
    private static final String userAgentName = "PeerList";
    private static final String appVersion = "0.1";
    private static final int version = 1;
    private static final int protocolVersion = 1;
    private static final int walletVersion = 0;

    protected NetworkParameters netParams;
    protected PeerGroup peerGroup;
    private int timeOffset = 0;
    private BigDecimal difficulty = new BigDecimal(0);

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

    @Override
    public ServerInfo getinfo() {
        // Dummy up a response for now.
        // Since ServerInfo is immutable, we have to build it entirely with the constructor.
        Coin balance = Coin.valueOf(0);
        boolean testNet = !netParams.getId().equals(NetworkParameters.ID_MAINNET);
        int keyPoolOldest = 0;
        int keyPoolSize = 0;
        return new ServerInfo(
                version,
                protocolVersion,
                walletVersion,
                balance,
                getblockcount(),
                timeOffset,
                getconnectioncount(),
                "proxy",
                difficulty,
                testNet,
                keyPoolOldest,
                keyPoolSize,
                Transaction.REFERENCE_DEFAULT_MIN_TX_FEE,
                Transaction.REFERENCE_DEFAULT_MIN_TX_FEE, // relayfee
                "no errors"                               // errors
        );
    }

}
