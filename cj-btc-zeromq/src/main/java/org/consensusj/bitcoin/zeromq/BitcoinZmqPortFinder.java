package org.consensusj.bitcoin.zeromq;

import com.msgilligan.bitcoinj.json.pojo.ZmqNotification;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
                .filter(n -> n.getType().equals("pub" + topic.toString()))
                .findFirst()
                .map(ZmqNotification::getAddress)
                .map(this::mapUri);
    }
    
    private synchronized void getNotifications() {
        if (notifications == null) {
            try {
                client.waitForServer(120);
                notifications = client.getZmqNotifications();
            } catch (IOException e) {
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
