package com.msgilligan.peerlist.model;

import org.bitcoinj.core.Peer;

import java.net.InetSocketAddress;

/**
 * PeerInfo
 * <p>
 * Simple Bean Constructed from Peer class for serialization over STOMP
 */
@Deprecated
public class PeerInfo {
    private InetSocketAddress socketAddress;

    public PeerInfo(Peer peer) {
        this.socketAddress = peer.getAddress().toSocketAddress();
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

}
