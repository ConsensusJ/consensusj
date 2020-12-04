package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.rpc.RpcURI;
import io.reactivex.rxjava3.disposables.Disposable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.consensusj.bitcoin.rx.BlockUtil;

import static java.lang.System.out;

/**
 * Sample Bitcoin ZMQ client
 */
public class SampleBitcoinZmqClient {
    static final int duration = 24 * 60 * 60 * 1000; // 1 day

    public static void main(String[] args) throws Exception {
        String rpcUser = "";
        String rpcPassword = "";
        
        RxBitcoinZmqService client = new RxBitcoinZmqService(MainNetParams.get(), RpcURI.getDefaultMainNetURI(), rpcUser, rpcPassword);

        try (client) {
            Disposable dBlock = client.observableBlock()
                    .subscribe(SampleBitcoinZmqClient::onBlock);
            Disposable dRawTx = client.observableTransaction()
                    .subscribe(SampleBitcoinZmqClient::onTx);
            Thread.sleep(duration);
        }
    }

    static void onBlock(Block block) {
        out.printf("Raw Block: %s/%s containing %s transactions\n",
                BlockUtil.blockHeightFromCoinbase(block),
                block.getHash(),
                block.getTransactions().size());
    }

    static void onTx(Transaction tx) {
        out.printf("Raw Tx: %s, value: %s\n",
                tx.getTxId(),
                tx.getOutputSum().toPlainString());
    }
}
