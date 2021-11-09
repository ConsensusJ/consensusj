package org.consensusj.bitcoin.rx.jsonrpc;

import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.consensusj.bitcoin.rx.jsonrpc.service.TxOutSetService;

import java.net.URI;

import static java.lang.System.out;

/**
 * Sample command-line tool that subscribes to ChainTip and TxOutSetInfo
 */
public class TxOutSetWatcherSample {
    public static void main(String[] args) throws InterruptedException {
        NetworkParameters networkParameters = MainNetParams.get();
        String rpcUser = "bitcoinrpc";
        String rpcPassword = "pass";
        URI rpcUri = URI.create("http://localhost:8332");
        boolean useZmq = true;

        try (   RxBitcoinClient client = new RxBitcoinClient(networkParameters, rpcUri, rpcUser, rpcPassword, useZmq);
                TxOutSetService txOutSetService = new TxOutSetService(client) ) {

            // Subscribe to ChainTips
            client.chainTipPublisher()
                    .subscribe(TxOutSetWatcherSample::onChainTip, TxOutSetWatcherSample::onError);

            // Blocking subscribe to TxOutSetInfo
            txOutSetService.getTxOutSetPublisher()
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
