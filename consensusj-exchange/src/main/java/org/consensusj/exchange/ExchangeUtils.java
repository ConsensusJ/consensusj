/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
