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
package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.json.conversion.HexUtil;

/**
 *
 */
public class SignedRawTransaction {
    private final byte[] bytes;
    private final boolean complete;

    public static SignedRawTransaction of(Transaction transaction) {
        return new SignedRawTransaction(transaction.serialize(), true);
    }

    @JsonCreator
    public SignedRawTransaction(@JsonProperty("hex")        String   hex,
                                @JsonProperty("complete")   boolean  complete) {
        this(HexUtil.hexStringToByteArray(hex), complete);
    }

    private SignedRawTransaction(byte[] bytes, boolean complete) {
        this.bytes = bytes;
        this.complete = complete;
    }

    public  String getHex() {
        return HexUtil.bytesToHexString(bytes);
    }

    public boolean isComplete() {
        return complete;
    }
}
