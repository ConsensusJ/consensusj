/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoin.rx.jsonrpc;

import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo;
import org.consensusj.bitcoin.rx.jsonrpc.service.TxOutSetService;

import java.net.URI;
import java.time.Duration;

import static java.lang.System.out;

/**
 * Sample command-line tool that subscribes to ChainTip and TxOutSetInfo
 */
public class TxOutSetWatcherSample {
    public static void main(String[] args) throws InterruptedException {
        Network network = BitcoinNetwork.MAINNET;
        String rpcUser = "bitcoinrpc";
        String rpcPassword = "pass";
        URI rpcUri = URI.create("http://localhost:8332");
        boolean useZmq = true;

        try (   RxBitcoinClient client = new RxBitcoinClient(network, rpcUri, rpcUser, rpcPassword, useZmq);
                TxOutSetService txOutSetService = new TxOutSetService(client, client.chainTipPublisher()) ) {

            client.connectToServer(Duration.ofMinutes(5)).join();
            // Subscribe to ChainTips
            Flowable.fromPublisher(client.chainTipPublisher())
                    .subscribe(TxOutSetWatcherSample::onChainTip, TxOutSetWatcherSample::onError);

            // Blocking subscribe to TxOutSetInfo
            Flowable.fromPublisher(txOutSetService.getTxOutSetPublisher())
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
