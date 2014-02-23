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

import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.msgilligan.peerlist.model.PeerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.google.bitcoin.core.*;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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
        peerGroup.startAndWait();
    }

    public NetworkParameters getNetworkParameters() {
        return this.netParams;
    }

    public void listPeers(Principal principal) {
        List<PeerInfo> peerInfos = new ArrayList<PeerInfo>();
        List<Peer> peers = peerGroup.getConnectedPeers();

        for (Peer peer : peers) {
            InetSocketAddress addr = peer.getAddress().toSocketAddress();
            PeerInfo info = new PeerInfo();
            info.setSocketAddress(addr);
            peerInfos.add(info);
        }
        this.messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/peers", peerInfos);
    }

}
