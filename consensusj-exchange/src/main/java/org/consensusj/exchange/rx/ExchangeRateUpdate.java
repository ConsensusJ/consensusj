package org.consensusj.exchange.rx;

import org.consensusj.exchange.CurrencyUnitPair;

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
}
