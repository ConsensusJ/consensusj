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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

/**
 * Configuration record for the daemon. Typically read from a TOML file.
 * @param networkId  name (see {@link BitcoinNetwork#toString()} or ID string of network (see {@link BitcoinNetwork#id()}
 * @param serverPort Port Daemon will listen on
 * @param dataDir Path to data directory
 * @param walletBaseName Basename of wallet files (basename-net.wallet and basename-net.spvchain)
 */
@ConfigurationProperties("walletd.config")
public record BitcoinDaemonConfig(@Nullable String networkId, int serverPort, @Nullable Path dataDir,
                                  @Bindable(defaultValue = "walletd") String walletBaseName)
{
    /**
     * @return Defaults to {@link BitcoinNetwork#MAINNET}
     * @throws IllegalArgumentException if {@link #networkId} isn't a valid name or id string.
     */
    public Network network() {
        return networkId == null || networkId.isBlank()
                ? BitcoinNetwork.MAINNET
                : BitcoinNetwork.fromString(networkId).orElseGet(
                            () -> BitcoinNetwork.fromIdString(networkId)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid network: " + networkId))
                        );
    }
}
