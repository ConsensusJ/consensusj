package com.msgilligan.bitcoinj.boot;

import com.msgilligan.peerlist.service.PeerService;

/**
 *
 */
public class BitcoinJsonRpcImpl implements BitcoinJsonRpc {
    private PeerService peerService;

    public BitcoinJsonRpcImpl(PeerService peerService) {
        this.peerService = peerService;
    }

    @Override
    public Integer getblockcount() {
        return peerService.getBlockCount();
    }

    @Override
    public Integer getconnectioncount() {
        return peerService.getConnectionCount();
    }
}
