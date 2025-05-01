package org.consensusj.jsonrpc.cli.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class XdgDataDir {
    enum OS {
        LINUX, MACOS, WINDOWS, UNKNOWN;
        static OS get() {
            String property = System.getProperty("os.name", "").toLowerCase(Locale.US);
            if (property.isEmpty())
                return OS.UNKNOWN;
            else if (property.contains("linux"))
                return OS.LINUX;
            else if (property.contains("win"))
                return OS.WINDOWS;
            else if (property.contains("mac"))
                return OS.MACOS;
            else
                return OS.UNKNOWN;
        }
    }

    /**
     * Return the Path to an XDG-style application configuration directory without making
     * sure it exists or creating it. (No disk I/O)
     *
     * @param appName The name of the current application
     * @return Path to the application configuration directory
     */
    public static Path getPath(String appName) {
        return switch(OS.get()) {
            case LINUX, MACOS, UNKNOWN -> getXdgConfigDir(appName);
            case WINDOWS -> Path.of(System.getenv("APPDATA"), appName.toLowerCase());
        };
    }

    /**
     * Path to an app-specific configuration directory
     * @param appName application name
     * @return path to application's configuration directory
     */
    public static Path getXdgConfigDir(String appName) {
        return getXdgConfigHome().resolve(appName.toLowerCase());
    }

    /**
     * Return {@code $XDG_CONFIG_HOME} if defined, or  {@code $HOME/.config} otherwise.
     * @return path to XDG configuration base directory
     */
    public static Path getXdgConfigHome() {
        String xdgConfigHomeEnv = System.getenv("XDG_CONFIG_HOME");
        return (xdgConfigHomeEnv != null && !xdgConfigHomeEnv.isEmpty())
                ? Paths.get(xdgConfigHomeEnv)
                : Paths.get(System.getProperty("user.home"), ".config");
    }
}
