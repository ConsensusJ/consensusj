package org.consensusj.daemon.micronaut;

import com.fasterxml.jackson.databind.Module;
import org.consensusj.bitcoin.json.conversion.RpcServerModule;
import io.micronaut.context.annotation.Factory;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.File;

/**
 * Initialize bitcoinj components, etc.
 */
@Factory
public class BitcoinFactory {
    private static Logger log = LoggerFactory.getLogger(BitcoinFactory.class);

    @Singleton
    public NetworkParameters networkParameters() {
        log.info("Returning NetworkParameters bean");
        return MainNetParams.get();
    }

    @Singleton
    public WalletAppKit getKit(NetworkParameters params) {
        log.info("Returning WalletAppKit bean");
        // TODO: make File(".") and filePrefix configurable
        File directory = new File(".");
        String filePrefix = "BitcoinJDaemon";

        return new WalletAppKit(params, directory, filePrefix);
    }

    @Singleton
    public Module jacksonModule() {
        return new RpcServerModule(null);
    }
}
