package com.msgilligan.bitcoinj.rpc.bitcoind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Currently focused on just getting connection info
 */
public class BitcoinConfFile {
    private File file;

    public BitcoinConfFile(File confFile) {
        file = confFile;
    }

    //

    /**
     * read a bitcoin.conf file
     * NOTE: Should we really use ugly JDK6 I/O here?
     * TODO: Improved error handling, currently returns defaults if any error
     * @return The configuration object
     */
    public BitcoinConf read()  {
        BitcoinConf conf = new BitcoinConf();
        setDefaults(conf);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return conf;
        }
        String line;
        try {
            while((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String trimmed = line.split("#")[0].trim();
                    String[] kv = trimmed.split("=");
                    if (kv.length == 2) {
                        conf.put(kv[0], kv[1]);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return conf;
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
