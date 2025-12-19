package org.consensusj.daemon.micronaut;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.bitcoinj.kits.WalletAppKit;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

// TODO: Rather than subclass WalletAppKitService there should be a RPCServerShutdown class or interface
// that is passed in to  WalletAppKitService that it can all when `stop` is called.
/**
 * Subclass of {@link WalletAppKitService} that implements the {@code stop} JSON-RPC method using
 * Micronaut {@link EmbeddedServer#stop()}
 */
@Singleton
public class MicronautWalletAppKitService extends WalletAppKitService {
    private static final Logger log = LoggerFactory.getLogger(MicronautWalletAppKitService.class);

    private EmbeddedServer embeddedServer;

    public MicronautWalletAppKitService(WalletAppKit kit) {
        super(kit);
    }
    
    @EventListener
    public void onStartup(ServerStartupEvent event) {
        log.info("Saving reference to embeddedServer");
        embeddedServer = event.getSource();
    }

    @EventListener
    public void onShutdown(ServerShutdownEvent event) {
        log.info("Shutting down");
        this.close();
    }

    /**
     * Initiate server shutdown. This is a JSON-RPC method and will initiate but not
     * complete server-shutdown because it must return a response to the client.
     * @return A status string indicating the server is stopping
     */
    @Override
    public CompletableFuture<String> stop() {
        log.info("stop");
        embeddedServer.stop();
        var appName = embeddedServer.getApplicationConfiguration().getName().orElse("server");
        return result(appName + " stopping");
    }
}
