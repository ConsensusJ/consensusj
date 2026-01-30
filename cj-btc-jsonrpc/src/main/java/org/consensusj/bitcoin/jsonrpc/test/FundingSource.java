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
package org.consensusj.bitcoin.jsonrpc.test;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

/**
 * A source of Bitcoin funds for testing
 *
 * In RegTest mode, it can be a RegTestFundingSource that mines coins in RegTest mode and sends them
 * to a requesting address. In other modes it can be a TestWallet preloaded with a certain amount of coins.
 */
public interface FundingSource {
    Sha256Hash requestBitcoin(Address toAddress, Coin requestedAmount) throws Exception;
    Address createFundedAddress(Coin amount) throws Exception;

    /**
     * An opportunity to do any necessary housekeeping. (e.g. consolidation)
     */
    void fundingSourceMaintenance();
}
