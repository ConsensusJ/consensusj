package org.consensusj.daemon.micronaut;

import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import com.msgilligan.bitcoinj.rpcserver.BitcoinJsonRpc;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Copy/paste from code in bitcoinj-server until we get
 * Micronaut DI config set up
 */
public class BitcoinImpl extends AbstractJsonRpcService implements BitcoinJsonRpc {
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());
    private static final int version = 1;
    private static final int protocolVersion = 1;
    private static final int walletVersion = 0;
    private int timeOffset = 0;
    private BigDecimal difficulty = new BigDecimal(0);

    public BitcoinImpl() {
        super(methods);
    }

    @Override
    public Integer getblockcount() {
        return 99;
    }

    @Override
    public Integer getconnectioncount() {
        return 3;
    }

    @Override
    public ServerInfo getinfo() {
        // Dummy up a response for now.
        // Since ServerInfo is immutable, we have to build it entirely with the constructor.
        Coin balance = Coin.valueOf(0);
        boolean testNet = true;
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
