package com.msgilligan.peerlist.model;

import java.net.InetSocketAddress;

/**
 * User: sean
 * Date: 2/23/14
 * Time: 12:18 AM
 */
public class PeerInfo {
    private InetSocketAddress socketAddress;

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

}
