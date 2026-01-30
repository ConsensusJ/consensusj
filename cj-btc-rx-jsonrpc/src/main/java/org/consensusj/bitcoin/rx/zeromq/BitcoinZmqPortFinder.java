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

import org.consensusj.bitcoin.json.pojo.ZmqNotification;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Utility to use Bitcoin Core JSON-RPC to find configured ZMQ notification ports.
 */
public class BitcoinZmqPortFinder {
    private final BitcoinClient client;
    private final URI rpcUri;
    private List<ZmqNotification> notifications;

    public BitcoinZmqPortFinder(BitcoinClient client) {
        this.client = client;
        rpcUri = client.getServerURI();
    }

    public Optional<URI> findPort(BitcoinZmqMessage.Topic topic) {
        getNotifications();
        return notifications.stream()
                .filter(n -> n.type().equals("pub" + topic.toString()))
                .findFirst()
                .map(ZmqNotification::address)
                .map(this::mapUri);
    }
    
    private synchronized void getNotifications() {
        if (notifications == null) {
            try {
                // TODO: We don't want to hang here for 120 seconds or so waiting for a server
                // client.waitForServer(Duration.ofSeconds(1));
                notifications = client.getZmqNotifications();
            } catch (IOException e) {
                // TODO: Handle this more gracefully, without runtime exception
                throw new RuntimeException(e);
            }
        }
    }

    private URI mapUri(URI configuredZmqUri) {
        return zmqPort(rpcUri, configuredZmqUri);
    }

    /**
     * Create tcp URI for ZMQ using host of JSON-RPC client and port of URI returned with zmqnotifications
     *
     * @param client http URI of JSON-RPC server
     * @param zmqPubURI ZMQ tcp URI returned with getzmqnotifications
     * @return A tcp URI for ZMQ using host from client port from zmqPubURI
     */
    private static URI zmqPort(URI client, URI zmqPubURI) {
        URI updated;
        try {
            updated = new URI("tcp", null, client.getHost(), zmqPubURI.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return updated;
    }

}
