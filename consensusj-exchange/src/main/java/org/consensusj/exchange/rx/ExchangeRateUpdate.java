package org.consensusj.exchange.rx;

import org.consensusj.exchange.CurrencyUnitPair;
import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.NumberValue;

/**
 * 
 */
public class ExchangeRateUpdate {
    public final CurrencyUnitPair pair;
    public final NumberValue currentFactor;
    public final long serverTimeStamp;
    //public final long clientTimeStamp; // ??
    //ExchangeRate rate;  // Should we have this?
    //Other "Ticker" information?
    
    public ExchangeRateUpdate(CurrencyUnitPair pair, NumberValue currentFactor, long serverTimeStamp) {
        this.pair = pair;
        this.currentFactor = currentFactor;
        this.serverTimeStamp = serverTimeStamp;
    }

    /**
     * Create an ExchangeRateValue for "unknown" exchange rate (no data yet)
     *
     * @param pair Exchange pair we don't have information on
     * @return An ExchangeRateUpdate with "unknown" values
     */
    public static ExchangeRateUpdate unknown(CurrencyUnitPair pair) {
        return new ExchangeRateUpdate(pair, DefaultNumberValue.of(0), 0);
    }
}
