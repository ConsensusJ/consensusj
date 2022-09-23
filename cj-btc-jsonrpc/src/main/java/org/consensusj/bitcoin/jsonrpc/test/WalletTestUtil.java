package org.consensusj.bitcoin.jsonrpc.test;

import java.util.Random;

/**
 * Utilities for integration testing server-side wallets
 */
public class WalletTestUtil {

    /**
     * Generate a valid, random wallet name. This is useful in integration testing.
     *
     * @return A wallet name of the form: <i>testwallet</i>-<i>ten-randomChars</i>
     */
    public static String randomWalletName() {
        return randomWalletName("testwallet", 10);
    }

    /**
     * Generate a valid, random wallet name. This is useful in integration testing.
     *
     * @param prefix Prefix, e.g. {@code "testwallet"}
     * @param numRandomChars The number of random characters to add to the prefix
     * @return A wallet name of the form: <i>prefix</i>-<i>randomChars</i>
     */
    public static String randomWalletName(String prefix, int numRandomChars) {
        return String.format("%s-%s", prefix, generateRandomName(numRandomChars));
    }

    static String generateRandomName(int targetStringLength) {
        int leftLimit = 97;     // letter 'a'
        int rightLimit = 122;   // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
