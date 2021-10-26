package org.consensusj.bitcoin.rx.zeromq;

import com.msgilligan.bitcoinj.json.pojo.ChainTip;
import io.reactivex.rxjava3.disposables.Disposable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.consensusj.bitcoin.util.BlockUtil;

import java.net.URI;

import static java.lang.System.out;

/**
 * Sample Bitcoin ZMQ client
 */
public class SampleBitcoinZmqClient {

    public static void main(String[] args) {
        NetworkParameters networkParameters = MainNetParams.get();
        String rpcUser = "";
        String rpcPassword = "";
        URI rpcUri = URI.create("http://localhost:8332");

        try (RxBitcoinZmqService client = new RxBitcoinZmqService(networkParameters, rpcUri, rpcUser, rpcPassword)) {
            // Subscribe to Blocks
            Disposable disposable = client.blockPublisher()
                    .subscribe(SampleBitcoinZmqClient::onBlock, SampleBitcoinZmqClient::onError);

            // Subscribe to ChainTips
            Disposable disposable2 = client.chainTipPublisher()
                    .subscribe(SampleBitcoinZmqClient::onChainTip, SampleBitcoinZmqClient::onError);


            // Blocking subscribe to Transactions (so main() doesn't finish)
            client.transactionPublisher()
                    .blockingSubscribe(SampleBitcoinZmqClient::onTx, SampleBitcoinZmqClient::onError);

            disposable.dispose();
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

    static void onChainTip(ChainTip tip) {
        out.printf("Chain tip: %s/%s\n", tip.getHeight(), tip.getHash());
    }

    static void onTx(Transaction tx) {
        out.printf("Raw Tx: %s, value: %s\n",
                tx.getTxId(),
                tx.getOutputSum().toPlainString());
    }

    static void onError(Throwable t) {
        out.println("Error: " + t);
    }

}
