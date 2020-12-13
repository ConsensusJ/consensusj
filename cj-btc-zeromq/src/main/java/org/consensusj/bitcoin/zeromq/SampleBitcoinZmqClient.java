package org.consensusj.bitcoin.zeromq;

import io.reactivex.rxjava3.disposables.Disposable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.consensusj.bitcoin.rx.BlockUtil;

import java.net.URI;

import static java.lang.System.out;

/**
 * Sample Bitcoin ZMQ client
 */
public class SampleBitcoinZmqClient {
    static final int duration = 24 * 60 * 60 * 1000; // 1 day

    public static void main(String[] args) throws Exception {
        String rpcUser = "";
        String rpcPassword = "";

        RxBitcoinZmqService client = new RxBitcoinZmqService(MainNetParams.get(), URI.create("http://localhost:8332"), rpcUser, rpcPassword);

        try (client) {
            Disposable dBlock = client.blockPublisher()
                    .subscribe(SampleBitcoinZmqClient::onBlock, SampleBitcoinZmqClient::onError);
            Disposable dRawTx = client.transactionPublisher()
                    .subscribe(SampleBitcoinZmqClient::onTx, SampleBitcoinZmqClient::onError);
            Thread.sleep(duration);
        }
    }

    static void onBlock(Block block) {
        out.println("Raw block");
        out.printf("Block version: %s\n", block.getVersion());
        out.printf("Raw Block: %s/%s containing %s transactions\n",
                BlockUtil.blockHeightFromCoinbase(block),
                block.getHash(),
                block.getTransactions().size());

    }

    static void onError(Throwable t) {
        out.println("Error: " + t);
    }

    static void onTx(Transaction tx) {
        out.printf("Raw Tx: %s, value: %s\n",
                tx.getTxId(),
                tx.getOutputSum().toPlainString());
    }
}
