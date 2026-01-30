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
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed information about an in-wallet transaction (from gettransaction RPC)
 */
public class WalletTransactionInfo {
    private final Coin amount;
    private final Coin fee;
    private final int confirmations;
    private final Sha256Hash blockhash;
    private final int blockindex;
    private final Instant blocktime;
    private final Sha256Hash txid;
    private final WalletConflictList walletconflicts;
    private final Instant time;
    private final Instant timereceived;
    private final String bip125Replaceable;
    private final DetailList details;
    private final String hex;
    private final RawTransactionInfo decoded;

    @JsonCreator
    public WalletTransactionInfo(@JsonProperty("amount")            Coin amount,
                                 @JsonProperty("fee")               Coin fee,
                                 @JsonProperty("confirmations")     int confirmations,
                                 @JsonProperty("blockhash")         Sha256Hash blockhash,
                                 @JsonProperty("blockindex")        int blockindex,
                                 @JsonProperty("blocktime")         long blocktime,
                                 @JsonProperty("txid")              Sha256Hash txid,
                                 @JsonProperty("walletconflicts")   WalletConflictList walletconflicts,
                                 @JsonProperty("time")              long time,
                                 @JsonProperty("timereceived")      long timereceived,
                                 @JsonProperty("bip125-replaceable") String bip125Replaceable,
                                 @JsonProperty("details")           DetailList details,
                                 @JsonProperty("hex")               String hex,
                                 @JsonProperty("decoded")           RawTransactionInfo decoded) {
        this.amount = amount;
        this.fee = fee;
        this.confirmations = confirmations;
        this.blockhash = blockhash;
        this.blockindex = blockindex;
        this.blocktime = Instant.ofEpochSecond(blocktime);
        this.txid = txid;
        this.walletconflicts = walletconflicts;
        this.time = Instant.ofEpochSecond(time);
        this.timereceived = Instant.ofEpochSecond(timereceived);
        this.bip125Replaceable = bip125Replaceable;
        this.details = details;
        this.hex = hex;
        this.decoded = decoded;
    }

    public Coin getAmount() {
        return amount;
    }

    public Coin getFee() {
        return fee;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public Sha256Hash getBlockhash() {
        return blockhash;
    }

    public int getBlockindex() {
        return blockindex;
    }

    public Instant getBlocktime() {
        return blocktime;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public List<Sha256Hash> getWalletconflicts() {
        return walletconflicts;
    }

    public Instant getTime() {
        return time;
    }

    public Instant getTimereceived() {
        return timereceived;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public String getHex() {
        return hex;
    }

    public RawTransactionInfo getDecoded() {
        return decoded;
    }

    public static class WalletConflictList extends ArrayList<Sha256Hash> {
    }

    public static class DetailList extends ArrayList<Detail> {
    }

    public static class Detail {
        private final String account;
        private final Address address;
        private final String category;
        private final Coin amount;
        private final String label;
        private final int vout;
        private final Coin fee;

        public Detail(@JsonProperty("account")  String account,
                      @JsonProperty("address")  Address address,
                      @JsonProperty("category") String category,
                      @JsonProperty("amount")   Coin amount,
                      @JsonProperty("label")    String label,
                      @JsonProperty("vout")     int vout,
                      @JsonProperty("fee")      Coin fee) {
            this.account = account;
            this.address = address;
            this.category = category;
            this.amount = amount;
            this.label = label;
            this.vout = vout;
            this.fee = fee;
        }

        public String getAccount() {
            return account;
        }

        public Address getAddress() {
            return address;
        }

        public String getCategory() {
            return category;
        }

        public Coin getAmount() {
            return amount;
        }

        public int getVout() {
            return vout;
        }

        public Coin getFee() {
            return fee;
        }
    }
}
