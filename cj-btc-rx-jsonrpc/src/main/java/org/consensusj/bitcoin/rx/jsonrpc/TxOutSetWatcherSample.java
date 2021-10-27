package org.consensusj.bitcoin.rx.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.net.URI;

import static java.lang.System.out;

/**
 *
 */
public class TxOutSetWatcherSample {
    public static void main(String[] args) throws InterruptedException {
        NetworkParameters networkParameters = MainNetParams.get();
        String rpcUser = "bitcoinrpc";
        String rpcPassword = "pass";
//        URI rpcUri = URI.create("http://192.168.8.50:8332");
        URI rpcUri = URI.create("http://localhost:8332");

        try (RxBitcoinClient client = new RxBitcoinClient(networkParameters, rpcUri, rpcUser, rpcPassword)) {

            // Subscribe to ChainTips
            client.chainTipPublisher()
                    .subscribe(TxOutSetWatcherSample::onChainTip, TxOutSetWatcherSample::onError);

            client.pollOnNewBlock(client::getTxOutSetInfo)
                    .blockingSubscribe(TxOutSetWatcherSample::onTxOutSetInfo, TxOutSetWatcherSample::onError);

        }
    }

    static void onChainTip(ChainTip tip) {
        out.printf("Chain tip: %s/%s\n", tip.getHeight(), tip.getHash());
    }

    static void onTxOutSetInfo(TxOutSetInfo info) {
        out.printf("TxOutSetInfo: %s/%s, utxos: %s, total BTC: %s\n",
                info.getHeight(),
                info.getBestBlock(),
                info.getTxOuts(),
                info.getTotalAmount().toFriendlyString());
    }

    static void onError(Throwable t) {
        out.println("Error: " + t);
    }
}
