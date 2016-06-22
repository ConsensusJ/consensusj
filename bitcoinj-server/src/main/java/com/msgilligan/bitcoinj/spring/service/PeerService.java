/**
 * Copyright 2014 Micheal Sean Gilligan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msgilligan.bitcoinj.spring.service;

import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import org.bitcoinj.core.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.security.Principal;
import java.util.List;

/**
 * Extend PeerGroupService with additional HTTP and WebSocket/STOMP services
 * for Peers and Transactions
 */
@Named
public class PeerService extends PeerGroupService {
    private final SimpMessageSendingOperations messagingTemplate;

    @Inject
    public PeerService(NetworkParameters params,
                       PeerDiscovery peerDiscovery,
                       SimpMessageSendingOperations messagingTemplate) {
        super(params, peerDiscovery);
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    @Override
    public void start() {
        super.start();
        peerGroup.addOnTransactionBroadcastListener(new MyPeerEventListener() );
    }


    public List<Peer> getPeers() {
        List<Peer> peers = peerGroup.getConnectedPeers();
        return peers;
    }

    public void listPeers(Principal principal) {
        List<Peer> peers = peerGroup.getConnectedPeers();
        this.messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/peers", peers);
    }

    void onPGTransaction(Peer peer, Transaction tx) {
        this.messagingTemplate.convertAndSend("/topic/tx", tx);
    }

    private class MyPeerEventListener implements OnTransactionBroadcastListener {
        @Override
        public void onTransaction(Peer peer, Transaction tx) {
            System.out.println("Got transaction: " + tx.getHashAsString());
            onPGTransaction(peer, tx);
        }
    }
}
