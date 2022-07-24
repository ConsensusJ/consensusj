package org.consensusj.bitcoin.jsonrpc.bitcoind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bitcoinj.utils.AppDataDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;

/**
 * Utility class with (extremely limited) parsing of {@code bitcoin.conf}
 * <p>
 * Currently focused on just getting connection info
 * <p>
 * Currently ignores `[Sections]`.
 */
public class BitcoinConfFile {
    private static final Logger log = LoggerFactory.getLogger(BitcoinConfFile.class);
    private static final String BITCOINAPPNAME = "Bitcoin";
    private final File file;

    public BitcoinConfFile() {
        this(AppDataDirectory.getPath(BITCOINAPPNAME).resolve("bitcoin.conf").toFile());
    }

    public BitcoinConfFile(File confFile) {
        file = confFile;
    }

    public static BitcoinConf readDefaultConfig() {
        return new BitcoinConfFile().readWithFallback();
    }

    //

    /**
     * Read a `bitcoin.conf` file
     *
     * @return The configuration object
     */
    public BitcoinConf read() throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset() );
        return parseLines(lines);
    }

    /**
     * Try to read `.conf` file, fallback to defaults on error
     *
     * If any exception occurs we return default values
     * TODO: maybe we should only return defaults on fileNotFound or accessDenied, etc.
     * 
     * @return Configuration read or defaults if read error
     */
    public BitcoinConf readWithFallback() {
        BitcoinConf conf = new BitcoinConf();
        setDefaults(conf);

        try {
            conf = read();
        } catch (IOException e) {
            if (e instanceof NoSuchFileException) {
                log.warn("NoSuchFileException: " + file.getAbsolutePath()   );
            } else {
                log.error("Error reading " + file.getAbsolutePath() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return conf;
    }

    /**
     * Not-very-well tested parsing of Bitcoin.conf (or similar) file
     * 
     * @param lines An array of one-line-Strings from the file
     * @return a BitcoinConf object
     */
    private BitcoinConf parseLines(List<String> lines) {
        BitcoinConf conf = new BitcoinConf();
        for (String line : lines) {
            if (!line.startsWith("#")) {
                String trimmed = line.split("#")[0].trim();
                String[] kv = trimmed.split("=");
                if (kv.length == 2) {
                    conf.put(kv[0], kv[1]);
                }

            }
        }
        return conf;
    }

    private void setDefaults(BitcoinConf conf) {
        conf.put("rpcconnect", "127.0.0.1");
        conf.put("rpcport", "8332");
        conf.put("rpcuser", "");
        conf.put("rpcpassword", "");
    }
}
