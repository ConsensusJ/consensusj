package org.consensusj.daemon.micronaut

import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Network
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.ECKey
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.jsonrpc.JsonRpcStatusException
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * Basic tests of an extremely basic Bitcoin server
 */
@MicronautTest
class ApplicationSpec extends Specification {
    @Inject
    EmbeddedServer server

    @Shared
    Network network

    @Shared
    BitcoinExtendedClient client
    
    def setup() {
        client = new BitcoinExtendedClient(server.URI, "", "")
        network = client.getNetwork()   // This forces the JSON-Mappers to load  TODO: Make this automatic
    }

    void 'test it works'() {
        expect:
        server.running
        server.URI.getScheme() == "http"
        server.URI.getHost() == "localhost" || server.URI.getHost().startsWith("runner") // "runner" is GitlabCI
    }

    void 'getnetwork'() {
        when:
        Network net = client.getNetwork()

        then:
        net == network
    }

    void 'getblockcount request'() {
        when:
        var height = client.getBlockCount()

        then:
        height >= 0
    }

    void 'help request'() {
        when:
        var help = client.help()

        then:
        help != null
    }

    void 'getnewaddress request'() {
        when:
        var address = client.getNewAddress();

        then:
        address != null
    }

    void 'getbalance request'() {
        when:
        var balance = client.getBalance();

        then:
        balance != null
        balance >= Coin.ZERO
    }

    void 'sendtoaddress request'() {
        when:
        var txId = client.sendToAddress(randomAddress(), 1_000.satoshi);

        then:
        JsonRpcStatusException ex = thrown()
        ex.getMessage() == "Server exception: Not implemented yet"
    }

    private Address randomAddress() {
        return new ECKey().toAddress(ScriptType.P2WPKH, network)
    }
}
