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
package org.consensusj.bitcoin.rx.zeromq;

import io.reactivex.rxjava3.core.Flowable;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.pojo.ChainTip;
import io.reactivex.rxjava3.disposables.Disposable;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoinj.util.BlockUtil;

import java.net.URI;

import static java.lang.System.out;

/**
 * Sample Bitcoin ZMQ client
 */
public class SampleBitcoinZmqClient {

    public static void main(String[] args) {
        Network network = BitcoinNetwork.MAINNET;
        String rpcUser = "";
        String rpcPassword = "";
        URI rpcUri = URI.create("http://localhost:8332");

        try (RxBitcoinZmqService client = new RxBitcoinZmqService(network, rpcUri, rpcUser, rpcPassword)) {
            // Subscribe to Blocks
            Disposable disposable = Flowable.fromPublisher(client.blockPublisher())
                    .subscribe(SampleBitcoinZmqClient::onBlock, SampleBitcoinZmqClient::onError);

            // Subscribe to ChainTips
            Disposable disposable2 = Flowable.fromPublisher(client.chainTipPublisher())
                    .subscribe(SampleBitcoinZmqClient::onChainTip, SampleBitcoinZmqClient::onError);


            // Blocking subscribe to Transactions (so main() doesn't finish)
            Flowable.fromPublisher(client.transactionPublisher())
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
