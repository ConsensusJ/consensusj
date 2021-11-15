package org.consensusj.bitcoin.rpc.bitcoind;

import java.io.File;

/**
 * Utility class to find Application data directory.
 *
 * @deprecated Use {@link org.bitcoinj.utils.AppDataDirectory} instead
 */
@Deprecated
public class AppDataDirectory {
    private static final String osName = System.getProperty("os.name").toLowerCase();

    public static File forAppName(String appName) {
        final String applicationDataDirectoryName;

        // Locations are OS-dependent
        if (isWindows()) {
            // Windows
            applicationDataDirectoryName = System.getenv("APPDATA") + File.separator + appName;
        } else if (isMac()) {
            // TODO: Isn't there a way to do this to account for non-english systems?
            applicationDataDirectoryName = System.getProperty("user.home") + "/Library/Application Support/" + appName;
        } else {
            // Other (probably a Unix variant)
            // Keep a clean home directory by prefixing with "."
            applicationDataDirectoryName = System.getProperty("user.home") + "/." + appName.toLowerCase();
        }

        File applicationDataDirectory = new File(applicationDataDirectoryName);

        return applicationDataDirectory;

    }

    static private boolean isWindows() {
        return osName.contains("windows");
    }

    static private boolean isMac() {
        return osName.startsWith("mac") || osName.startsWith("darwin");
    }


}
