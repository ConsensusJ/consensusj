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
package org.consensusj.daemon.micronaut;

import com.fasterxml.jackson.databind.Module;
import io.micronaut.context.annotation.Bean;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.Context;
import org.bitcoinj.utils.AppDataDirectory;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.consensusj.bitcoin.json.conversion.RpcServerModule;
import io.micronaut.context.annotation.Factory;
import org.bitcoinj.kits.WalletAppKit;
import org.consensusj.bitcoin.services.WalletAppKitService;
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
        log.info("Creating WalletAppKit for {} <============", config.network().id());
        log.info("Returning WalletAppKit bean, wallet directory: {}, prefix: {}", dataDirectory.getAbsolutePath(), filePrefix);
        return new WalletAppKit((BitcoinNetwork) config.network(), ScriptType.P2PKH, KeyChainGroupStructure.BIP43, dataDirectory, filePrefix);
    }

    @Singleton
    @Bean(preDestroy = "close")
    public WalletAppKitService walletAppKitService(WalletAppKit walletAppKit, MicronautJsonRpcShutdownService shutdownService) {
        WalletAppKitService service = new WalletAppKitService(walletAppKit, shutdownService);
        service.start();
        return service;
    }

    @Singleton
    public Module jacksonModule() {
        return new RpcServerModule();
    }
}
