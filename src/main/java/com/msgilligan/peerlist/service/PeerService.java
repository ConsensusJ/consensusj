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
package com.msgilligan.peerlist.service;

import org.bitcoinj.net.discovery.PeerDiscovery;
import com.msgilligan.peerlist.model.PeerInfo;
import com.msgilligan.peerlist.model.TransactionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import org.bitcoinj.core.*;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A Service for maintaining Bitcoin peers
 */
@Service
public class PeerService {
    private static final String userAgentName = "PeerList";
    private static final String appVersion = "0.1";
    private NetworkParameters netParams;
    private PeerGroup peerGroup;
    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public PeerService(NetworkParameters params,
                       PeerDiscovery peerDiscovery,
                       SimpMessageSendingOperations messagingTemplate) {
        this.netParams = params;
        this.peerGroup = new PeerGroup(params);
        this.messagingTemplate = messagingTemplate;
        peerGroup.setUserAgent(userAgentName, appVersion);
        peerGroup.addPeerDiscovery(peerDiscovery);
    }

    @PostConstruct
    public void start() {
        peerGroup.startAsync();
        peerGroup.addEventListener(new MyPeerEventListener() );
    }

    public NetworkParameters getNetworkParameters() {
        return this.netParams;
    }

    public void listPeers(Principal principal) {
        List<Peer> peers = peerGroup.getConnectedPeers();
        List<PeerInfo> peerInfos = peers.stream().map(PeerInfo::new).collect(toList());
        this.messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/peers", peerInfos);
    }

    void onPGTransaction(Peer peer, Transaction t) {
        TransactionInfo tx = new TransactionInfo(t);
        this.messagingTemplate.convertAndSend("/topic/tx", tx);
    }

    private class MyPeerEventListener extends AbstractPeerEventListener {
        @Override
        public void onTransaction(Peer peer, Transaction t) {
            System.out.println("Got transaction: " + t.getHashAsString());
            onPGTransaction(peer, t);
        }
    }


}
