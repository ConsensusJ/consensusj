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
package com.msgilligan.bitcoinj.spring.services;

import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import org.bitcoinj.core.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.security.Principal;
import java.util.List;

/**
 * Use a PeerGroup to provide REST and WebSocket/STOMP services
 * for Peers and Transactions
 */
@Named
public class PeerStompService implements PeerConnectedEventListener, PeerDisconnectedEventListener, OnTransactionBroadcastListener {
    private static final Logger log = LoggerFactory.getLogger(PeerStompService.class);
    private final PeerGroup peerGroup;
    private final SimpMessageSendingOperations messagingTemplate;
    private final Context context;

    @Inject
    public PeerStompService(Context context,
                            PeerGroup peerGroup,
                            SimpMessageSendingOperations messagingTemplate) {
        this.peerGroup = peerGroup;
        this.context = context;
        this.messagingTemplate = messagingTemplate;
        //this.executorService = Executors.newFixedThreadPool(6, new ContextPropagatingThreadFactory("StompServiceListeners"));
        log.info("PeerStompService: Constructed with netparams: {}", context.getParams().getId());
    }

    @PostConstruct
    public void start() {
        log.info("PeerStompService: start()");
        log.debug("PeerStompService: context from thread is: {}", Context.get().getParams().getId());
//        peerGroup.start();    // Currently this is done in BitcoinConfig
        peerGroup.addConnectedEventListener(this);
        peerGroup.addDisconnectedEventListener(this);
        peerGroup.addOnTransactionBroadcastListener(this);
    }
    
    public NetworkParameters getNetworkParameters() {
        return context.getParams();
    }

    public List<Peer> getPeers() {
        return peerGroup.getConnectedPeers();
    }

    public void listPeers(Principal principal) {
        List<Peer> peers = peerGroup.getConnectedPeers();
        this.messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/peers", peers);
    }

    @Override
    public void onTransaction(Peer peer, Transaction tx) {
        log.info("PeerStompService: Got transaction: {}", tx.getTxId());
        this.messagingTemplate.convertAndSend("/topic/tx", tx);
    }

    @Override
    public void onPeerConnected(Peer peer, int peerCount) {
        log.info("PeerStompService: Peer Connected: {}, count: {}", peer.getAddress(), peerCount);
    }

    @Override
    public void onPeerDisconnected(Peer peer, int peerCount) {
        log.info("PeerStompService: Peer Disconnected, count: {}", peerCount);
    }
}
