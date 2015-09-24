package com.msgilligan.bitcoinj.json.conversion;

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
}
