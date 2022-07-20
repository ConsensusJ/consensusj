package org.consensusj.bitcoin.json.conversion;

import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utilities for Bitcoin Math
 * @deprecated All methods are obsoleted by methods in bitcoinj 0.16
 */
@Deprecated
public class BitcoinMath {
    public static final MathContext DEFAULT_CONTEXT = new MathContext(0, RoundingMode.UNNECESSARY);
    public static final int DEFAULT_SCALE = Coin.SMALLEST_UNIT_EXPONENT;
    public static final BigDecimal satoshiPerCoinDecimal = new BigDecimal(Coin.COIN.value, DEFAULT_CONTEXT);

    /**
     * Convert from BTC `BigDecimal` value to satoshi `long`.
     *
     * @param btc Bitcoin amount in BTC units
     * @return number of satoshi (long)
     * @deprecated Use {@link Coin#btcToSatoshi(BigDecimal)}
     */
    @Deprecated
    public static long btcToSatoshi(final BigDecimal btc) {
        return Coin.btcToSatoshi(btc);
    }

    /**
     * Convert from satoshi `long` to BTC `BigDecimal`.
     *
     * @param satoshi number of satoshi (long)
     * @return Bitcoin amount in BTC units
     * @deprecated Use {@link Coin#satoshiToBtc(long)}
     */
    @Deprecated
    public static BigDecimal satoshiToBtc(final long satoshi) {
        return Coin.satoshiToBtc(satoshi);
    }

    /**
     * Convert from BTC `BigDecimal` value to `Coin` type.
     *
     * @param btc Bitcoin amount in BTC units
     * @return bitcoinj `Coin` type (uses satoshi unit internally)
     * @deprecated Use {@link Coin#ofBtc(BigDecimal)}
     */
    @Deprecated
    public static Coin btcToCoin(final BigDecimal btc) {
        return Coin.ofBtc(btc);
    }

    /**
     * Convert from `Coin` type to BTC `BigDecimal` value .
     *
     * @param coin Coin value to convert to BTC
     * @return  Bitcoin amount in BTC units
     * @deprecated Use {@link Coin#toBtc()}
     */
    @Deprecated
    public static BigDecimal coinToBTC(final Coin coin) {
         return coin.toBtc();
    }
}
