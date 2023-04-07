package org.consensusj.daemon.micronaut;

import com.fasterxml.jackson.databind.Module;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.consensusj.bitcoin.json.conversion.RpcServerModule;
import io.micronaut.context.annotation.Factory;
import org.bitcoinj.kits.WalletAppKit;
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
    public BitcoinNetwork network() {
        log.info("Returning NetworkParameters bean");
        return BitcoinNetwork.MAINNET;
    }

    @Singleton
    public WalletAppKit getKit(BitcoinNetwork network) {
        log.info("Returning WalletAppKit bean");
        // TODO: make File(".") and filePrefix configurable
        File directory = new File(".");
        String filePrefix = "BitcoinJDaemon";

        return new WalletAppKit(network, ScriptType.P2PKH, KeyChainGroupStructure.BIP32, directory, filePrefix);
    }

    @Singleton
    public Module jacksonModule() {
        return new RpcServerModule();
    }
}
