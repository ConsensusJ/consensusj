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

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;

/**
 * Interface for tests that use a BitcoinExtendedClient
 */
public interface BitcoinClientAccessor {

    /**
     * Preferred accessor
     * @return The Bitcoin Client
     */
    BitcoinExtendedClient client();

    /**
     * JavaBeans style getter/accessor (for Groovy, etc)
     * @return The Bitcoin Client
     */
    default BitcoinExtendedClient getClient() {
        return client();
    }
}
