package org.consensusj.exchange;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public interface ExchangeUtils {
    static List<CurrencyUnitPair> pairsConvert(String[] strings) {
        return pairsConvert(Arrays.asList(strings));
    }

    static List<CurrencyUnitPair> pairsConvert(List<String> strings) {
        return strings.stream()
                .map(CurrencyUnitPair::new)
                .collect(Collectors.toList());
    }
}
