package org.consensusj.bitcoin.json.conversion;

import java.util.Formatter;

/**
 * Hex string to hex conversion utility class.
 */
public class HexUtil {
    /**
     * Converts a hex-encoded string into a byte array.
     *
     * @param s A string to convert
     * @return The byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // From: http://bitcoin.stackexchange.com/questions/8475/how-to-get-hex-string-from-transaction-in-bitcoinj
    public static String bytesToHexString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        formatter.close();
        return sb.toString();
    }
}
