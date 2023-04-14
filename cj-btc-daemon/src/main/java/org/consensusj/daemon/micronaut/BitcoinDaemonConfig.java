package org.consensusj.daemon.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;

import java.nio.file.Path;

/**
 * Configuration record for the daemon. Typically read from a TOML file.
 * @param networkId  name (see {@link BitcoinNetwork#toString()} or ID string of network (see {@link BitcoinNetwork#id()}
 * @param serverPort Port Daemon will listen on
 * @param dataDir Path to data directory
 * @param walletBaseName Basename of wallet files (basename-net.wallet and basename-net.spvchain)
 */
@ConfigurationProperties("cjbitcoind.config")
public record BitcoinDaemonConfig(String networkId, int serverPort, @Nullable Path dataDir,
                                  @Bindable(defaultValue = "CJBitcoinDaemon") String walletBaseName)
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
