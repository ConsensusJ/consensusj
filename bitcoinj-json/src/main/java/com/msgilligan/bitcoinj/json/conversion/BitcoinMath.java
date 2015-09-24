package com.msgilligan.bitcoinj.json.conversion;

import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utilities for Bitcoin Math
 */
public class BitcoinMath {
    public static final MathContext DEFAULT_CONTEXT = new MathContext(0, RoundingMode.UNNECESSARY);
    public static final int DEFAULT_SCALE = Coin.SMALLEST_UNIT_EXPONENT;
    public static final BigDecimal satoshiPerCoinDecimal = new BigDecimal(Coin.COIN.value, DEFAULT_CONTEXT);

    /**
     * Convert from BTC `BigDecimal` value to satoshi `long`.
     *
     * @param btc Bitcoin amount in BTC units
     * @return number of satoshi (long)
     */
    public static long btcToSatoshi(final BigDecimal btc) {
        BigDecimal satoshisDecimal = btc.multiply(satoshiPerCoinDecimal);
        return satoshisDecimal.longValueExact();
    }

    /**
     * Convert from satoshi `long` to BTC `BigDecimal`.
     *
     * @param satoshi number of satoshi (long)
     * @return Bitcoin amount in BTC units
     */
    public static BigDecimal satoshiToBtc(final long satoshi) {
        BigDecimal bdSatoshi = new BigDecimal(satoshi, BitcoinMath.DEFAULT_CONTEXT);
        return bdSatoshi.divide(satoshiPerCoinDecimal, DEFAULT_SCALE, RoundingMode.UNNECESSARY);
    }

    /**
     * Convert from BTC `BigDecimal` value to `Coin` type.
     *
     * @param btc Bitcoin amount in BTC units
     * @return bitcoinj `Coin` type (uses satoshi unit internally)
     */
    public static Coin btcToCoin(final BigDecimal btc) {
        return Coin.valueOf(btcToSatoshi(btc));
    }

    /**
     * Convert from `Coin` type to BTC `BigDecimal` value .
     *
     * @param coin Coin value to convert to BTC
     * @return  Bitcoin amount in BTC units
     */
    public static BigDecimal coinToBTC(final Coin coin) {
         return satoshiToBtc(coin.value);
    }
}
