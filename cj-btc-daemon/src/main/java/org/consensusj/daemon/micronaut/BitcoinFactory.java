package org.consensusj.daemon.micronaut;

import com.fasterxml.jackson.databind.Module;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.Context;
import org.bitcoinj.utils.AppDataDirectory;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.consensusj.bitcoin.json.conversion.RpcServerModule;
import io.micronaut.context.annotation.Factory;
import org.bitcoinj.kits.WalletAppKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Initialize bitcoinj components, etc.
 */
@Factory
public class BitcoinFactory {
    private static final Logger log = LoggerFactory.getLogger(BitcoinFactory.class);

    @Singleton
    public WalletAppKit getKit(BitcoinDaemonConfig config) throws IOException {
        if (!(config.network() instanceof BitcoinNetwork)) {
            throw new IllegalArgumentException("Unsupported network");
        }
        Context.propagate(new Context());
        String filePrefix = config.walletBaseName() + "-" + config.network();
        File dataDirectory = config.dataDir() == null
                ? config.network() == BitcoinNetwork.REGTEST
                    ? Files.createTempDirectory(config.walletBaseName()).toFile()  // null && regtest -> temporary dir
                    : AppDataDirectory.get(config.walletBaseName()).toFile()       // null -> app data dir
                : config.dataDir().toFile();                                       // non-null -> use Path from config
        log.info("Returning WalletAppKit bean, wallet directory: {}, prefix: {}", dataDirectory.getAbsolutePath(), filePrefix);
        return new WalletAppKit((BitcoinNetwork) config.network(), ScriptType.P2PKH, KeyChainGroupStructure.BIP32, dataDirectory, filePrefix);
    }

    @Singleton
    public Module jacksonModule() {
        return new RpcServerModule();
    }
}
