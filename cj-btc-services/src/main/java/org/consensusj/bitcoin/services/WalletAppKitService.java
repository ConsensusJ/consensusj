package org.consensusj.bitcoin.services;

import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;

/**
 * Implement a subset of Bitcoin JSON RPC using a WalletAppKit
 */
@Named
public class WalletAppKitService implements BitcoinJsonRpc {
    private static Logger log = LoggerFactory.getLogger(WalletAppKitService.class);
    private static final String userAgentName = "PeerList";
    private static final String appVersion = "0.1";
    private static final int version = 1;
    private static final int protocolVersion = 1;
    private static final int walletVersion = 0;

    protected NetworkParameters netParams;
    protected Context context;
    protected WalletAppKit kit;

    private int timeOffset = 0;
    private BigDecimal difficulty = new BigDecimal(0);

    @Inject
    public WalletAppKitService(NetworkParameters params,
                               Context context,
                               WalletAppKit kit) {
        this.netParams = params;
        this.context = context;
        this.kit = kit;
    }

    @PostConstruct
    public void start() {
        log.info("WalletAppKitService.start()");
        kit.setUserAgent(userAgentName, appVersion);
        kit.setBlockingStartup(false);
        kit.startAsync();
        //kit.awaitRunning();
    }

    public NetworkParameters getNetworkParameters() {
        return this.netParams;
    }

    public PeerGroup getPeerGroup() {
        kit.awaitRunning();
        return kit.peerGroup();
    }

    @Override
    public Integer getblockcount() {
        log.info("getblockcount");
        if(!kit.isRunning()) {
            log.warn("kit not running, returning null");
            return null;
        }
        return kit.chain().getChainHead().getHeight();
    }

    @Override
    public Integer getconnectioncount() {
        if(!kit.isRunning()) {
            return null;
        }
        try {
            return kit.peerGroup().numConnectedPeers();
        } catch (IllegalStateException ex) {
            return null;
        }
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
