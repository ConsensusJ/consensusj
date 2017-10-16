package com.msgilligan.bitcoinj.money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ExchangeRate;

/**
 * Currency Pair using JavaMoney CurrencyUnit type
 */
public class CurrencyUnitPair implements Comparable<CurrencyUnitPair> {

    final private CurrencyUnit base;
    final private CurrencyUnit target;

    /**
     * @param pair A string of the form "base/target"
     */
    public CurrencyUnitPair(String pair) {
        this(Monetary.getCurrency(pair.split("/")[0]), Monetary.getCurrency(pair.split("/")[1]));
    }

    /*
     * @param base base currency as a JavaMoney currency code
     * @param target base currency as a JavaMoney currency code
     */
    public CurrencyUnitPair(String base, String target) {
        this(Monetary.getCurrency(base), Monetary.getCurrency(target));
    }

    /*
     * @param base base currency as a JavaMoney CurrencyUnit
     * @param target base currency as a JavaMoney CurrencyUnit
     */
    public CurrencyUnitPair(CurrencyUnit base, CurrencyUnit target) {
        this.base = base;
        this.target = target;
    }

    /**
     * @param rate specifies the base and target currencies
     */
    public CurrencyUnitPair(ExchangeRate rate) {
        this(rate.getBaseCurrency(), rate.getCurrency());
    }

    /**
     * Get base CurrencyUnit
     * @return base CurrencyUnit
     */
    public CurrencyUnit getBase() {
        return base;
    }

    /**
     * Get target CurrencyUnit
     * @return target CurrencyUnit
     */
    public CurrencyUnit getTarget() {
        return target;
    }

    @Override
    public String toString() {

        return base.getCurrencyCode() + "/" + target.getCurrencyCode();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CurrencyUnitPair other = (CurrencyUnitPair) obj;
        if (base == null) {
            if (other.base != null) {
                return false;
            }
        } else if (!base.equals(other.base)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CurrencyUnitPair o) {

        return (base.compareTo(o.base) << 16) + target.compareTo(o.target);
    }
}
