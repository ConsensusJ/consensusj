package org.consensusj.bitcoin.json.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Address;
import org.bitcoinj.core.LockTime;
import org.bitcoinj.script.Script;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * RawTransaction POJO
 */
// "hash" property added (present in Bitcoin 0.13)
public class RawTransactionInfo {
    private final String hex;
    private final Sha256Hash txid;
    private final long version;
    private final LockTime lockTime;
    private final List<Vin> vin;
    private final List<Vout> vout;
    private final Sha256Hash blockhash;
    private final int confirmations;
    private final Instant time;
    public final Instant blocktime;

    @JsonCreator
    public RawTransactionInfo(@JsonProperty("hex")              String hex,
                              @JsonProperty("txid")             Sha256Hash txid,
                              @JsonProperty("version")          long version,
                              @JsonProperty("locktime")         long locktime,
                              @JsonProperty("vin")              VinList vin,
                              @JsonProperty("vout")             VoutList vout,
                              @JsonProperty("blockhash")        Sha256Hash blockhash,
                              @JsonProperty("confirmations")    int confirmations,
                              @JsonProperty("time")             long time,
                              @JsonProperty("blocktime")        long blocktime) {
        this.hex = hex;
        this.txid = txid;
        this.version = version;
        this.lockTime = LockTime.of(locktime);
        this.vin = vin;
        this.vout = vout;
        this.blockhash = blockhash;
        this.confirmations = confirmations;
        this.time = Instant.ofEpochSecond(time);
        this.blocktime = Instant.ofEpochSecond(blocktime);
    }

    /**
     * Construct from a bitcoinj transaction
     * @param transaction A bitcoinj confirmed or unconfirmed transaction
     */
    public RawTransactionInfo(Transaction transaction) {
        this.hex = HexUtil.bytesToHexString(transaction.bitcoinSerialize());
        this.txid = transaction.getTxId();
        this.version = transaction.getVersion();
        this.lockTime = transaction.lockTime();
        this.blockhash = null;  // For now
        this.confirmations = transaction.getConfidence().getDepthInBlocks();
        this.time = Instant.ofEpochSecond(0); // TODO: block header time of block including transaction
        this.blocktime = this.time; // same as time (see API doc)
        vin = new VinList();
        for (TransactionInput input : transaction.getInputs()) {
            vin.add(new Vin(txid,
                            input.getOutpoint().getIndex(),
                            input.getScriptSig().toString(),
                            input.getSequenceNumber()));
        }
        vout = new VoutList();
        for (TransactionOutput output : transaction.getOutputs()) {
            ScriptPubKeyInfo scriptPubKeyInfo = new ScriptPubKeyInfo(output.getScriptPubKey());
            vout.add(new Vout(output.getValue(),
                                output.getIndex(),
                                scriptPubKeyInfo));
        }
    }

    public String getHex() {
        return hex;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    public long getVersion() {
        return version;
    }

    public LockTime getLockTime() {
        return lockTime;
    }

    /**
     * @return lock time
     * @deprecated Use {@link #getLockTime()}
     */
    @Deprecated
    public long getLocktime() {
        return getLockTime().rawValue();
    }

    public List<Vin> getVin() {
        return vin;
    }

    public List<Vout> getVout() {
        return vout;
    }

    public Sha256Hash getBlockhash() {
        return blockhash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public Instant getTime() {
        return time;
    }

    public Instant getBlocktime() {
        return blocktime;
    }

    public static class VinList extends ArrayList<Vin> {
    }

    public static class VoutList extends ArrayList<Vout> {
    }

    public static class Vin {
        public final Sha256Hash txid;
        public final  long vout;
        public final  Object scriptSig;
        public final  long sequence;

        @JsonCreator
        public Vin(@JsonProperty("txid") Sha256Hash txid,
                   @JsonProperty("vout") long vout,
                   @JsonProperty("scriptSig") Object scriptSig,
                   @JsonProperty("sequence") long sequence) {
            this.txid = txid;
            this.vout = vout;
            this.scriptSig = scriptSig;
            this.sequence = sequence;
        }

        public Sha256Hash getTxid() {
            return txid;
        }

        public long getVout() {
            return vout;
        }

        public Object getScriptSig() {
            return scriptSig;
        }

        public long getSequence() {
            return sequence;
        }
    }

    public static class Vout {
        public final  Coin value;
        public final  int n;
        public final  ScriptPubKeyInfo scriptPubKey;

        @JsonCreator
        public Vout(@JsonProperty("value")          Coin value,
                    @JsonProperty("n")              int n,
                    @JsonProperty("scriptPubKey")   ScriptPubKeyInfo scriptPubKey) {
            this.value = value;
            this.n = n;
            this.scriptPubKey = scriptPubKey;
        }

        public Coin getValue() {
            return value;
        }

        public int getN() {
            return n;
        }

        public ScriptPubKeyInfo getScriptPubKey() {
            return scriptPubKey;
        }
    }

    public static class ScriptPubKeyInfo {
        private final String asm;
        private final String hex;
        private final int reqSigs;
        private final String type;
        private final List<Address> addresses;

        @JsonCreator
        public ScriptPubKeyInfo(@JsonProperty("asm") String asm,
                                @JsonProperty("hex") String hex,
                                @JsonProperty("reqSigs") int reqSigs,
                                @JsonProperty("type") String type,
                                @JsonProperty("addresses") List<Address> addresses) {
            this.asm = asm;
            this.hex = hex;
            this.reqSigs = reqSigs;
            this.type = type;
            this.addresses = addresses;
        }

        public ScriptPubKeyInfo(Script script) {
            this.asm = script.toString();
            this.hex = HexUtil.bytesToHexString(script.getProgram());
            this.reqSigs = -1;
            this.type = "bitcoinj Script (not-fully-supported)";
            this.addresses = List.of();
        }

        public String getAsm() {
            return asm;
        }

        public String getHex() {
            return hex;
        }

        public int getReqSigs() {
            return reqSigs;
        }

        public String getType() {
            return type;
        }

        public List<Address> getAddresses() {
            return addresses;
        }
    }
}
