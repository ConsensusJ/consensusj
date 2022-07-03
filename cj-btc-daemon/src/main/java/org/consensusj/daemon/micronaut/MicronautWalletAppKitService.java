package org.consensusj.daemon.micronaut;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.bitcoinj.kits.WalletAppKit;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Subclass of {@link WalletAppKitService} that implements the {@code stop} JSON-RPC method using
 * Micronaut {@link EmbeddedServer#stop()}
 */
@Singleton
public class MicronautWalletAppKitService extends WalletAppKitService {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcController.class);

    private EmbeddedServer embeddedServer;

    public MicronautWalletAppKitService(WalletAppKit kit) {
        super(kit);
    }
    
    @EventListener
    public void onStartup(ServerStartupEvent event) {
        log.info("Saving reference to embeddedServer");
        embeddedServer = event.getSource();
    }

    @Override
    public CompletableFuture<String> stop() {
        log.info("stop");
        embeddedServer.stop();
        return result("cj-btc-daemon stopping");
    }
}
