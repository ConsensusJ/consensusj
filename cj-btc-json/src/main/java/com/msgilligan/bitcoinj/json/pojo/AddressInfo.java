package com.msgilligan.bitcoinj.json.pojo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;

public class AddressInfo {
    private final Address address;
    private final String scriptPubKey;
    private final boolean ismine;
    private final boolean solvable;
    private final String desc;
    private final boolean iswatchonly;
    private final boolean isscript;
    private final boolean iswitness;
    private final String pubkey;
    private final boolean iscompressed;
    private final String label;
    private final boolean ischange;
    private final Long timestamp;
    private final List<Object> labels;

    /**
     *
     * @param address
     * @param ischange
     * @param ismine
     * @param label
     * @param iswatchonly
     * @param iswitness
     * @param labels
     * @param scriptPubKey
     * @param solvable
     * @param isscript
     * @param iscompressed
     * @param desc
     * @param pubkey
     * @param timestamp
     */
    public AddressInfo(@JsonProperty("address") Address address,
                       @JsonProperty("scriptPubKey") String scriptPubKey,
                       @JsonProperty("ismine") boolean ismine,
                       @JsonProperty("solvable") boolean solvable,
                       @JsonProperty("desc") String desc,
                       @JsonProperty("iswatchonly") boolean iswatchonly,
                       @JsonProperty("isscript") boolean isscript,
                       @JsonProperty("iswitness") boolean iswitness,
                       @JsonProperty("pubkey") String pubkey,
                       @JsonProperty("iscompressed") boolean iscompressed,
                       @JsonProperty("label") String label,
                       @JsonProperty("ischange") boolean ischange,
                       @JsonProperty("timestamp") Long timestamp,
                       @JsonProperty("labels") List<Object> labels) {
        super();
        this.address = address;
        this.scriptPubKey = scriptPubKey;
        this.ismine = ismine;
        this.solvable = solvable;
        this.desc = desc;
        this.iswatchonly = iswatchonly;
        this.isscript = isscript;
        this.iswitness = iswitness;
        this.pubkey = pubkey;
        this.iscompressed = iscompressed;
        this.label = label;
        this.ischange = ischange;
        this.timestamp = timestamp;
        this.labels = labels;
    }

    public Address getAddress() {
        return address;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public boolean getIsmine() {
        return ismine;
    }

    public boolean getSolvable() {
        return solvable;
    }

    public String getDesc() {
        return desc;
    }

    public boolean getIswatchonly() {
        return iswatchonly;
    }

    public boolean getIsscript() {
        return isscript;
    }

    public boolean getIswitness() {
        return iswitness;
    }

    public String getPubkey() {
        return pubkey;
    }

    public boolean getIscompressed() {
        return iscompressed;
    }

    public String getLabel() {
        return label;
    }

    public boolean getIschange() {
        return ischange;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public List<Object> getLabels() {
        return labels;
    }

    @Deprecated
    public static class Label {

        private final String name;
        private final String purpose;

        /**
         *
         * @param name Label name
         * @param purpose Label purpose ("send" or "receive")
         */
        public Label(@JsonProperty("name") String name, @JsonProperty("purpose") String purpose) {
            this.name = name;
            this.purpose = purpose;
        }

        public String getName() {
            return name;
        }

        public String getPurpose() {
            return purpose;
        }
    }
}
