package org.consensusj.namecoin.jsonrpc.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.util.Map;

/**
 * Namecoin name data
 *
 * example format:
 * [name:d/beelin,
 * value:{"alias":"beelin.github.io"},
 * txid:5737868f7044ade9b0c04698c563955d9b49db841a4e575bc384873073b956ed,
 * address:NAPwebo2VLvGdBFC4cHrLRPS6ZXPKddx9Z,
 * expires_in:31874]
 */
public class NameData {
    private static ObjectMapper mapper = new ObjectMapper();
    private static JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);


    private final   String name;
    private final   Map<String, Object> value;     // Deserialized from escape JSON string
    private final   Sha256Hash txid;
    private final   Address address;
    private final   int expires_in;

    @JsonCreator
    public NameData(@JsonProperty("name")       String name,
                    @JsonProperty("value")      String value,
                    @JsonProperty("txid")       Sha256Hash txid,
                    @JsonProperty("address")    Address address,
                    @JsonProperty("expires_in") int expires_in) throws IOException {
        this.name = name;
        this.value = mapper.readValue(value, mapType);
        this.txid = txid;
        this.address = address;
        this.expires_in = expires_in;
    }

    /**
     *
     * @return namespace/name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return JSONNode
     */
    public Map<String, Object> getValue() {
        return value;
    }

    public Sha256Hash getTxid() {
        return txid;
    }

    /**
     *
     * @return Address as String (for now since bitcoinj won't allow N... addresses)
     */
    public Address getAddress() {
        return address;
    }

    public int getExpires_in() {
        return expires_in;
    }
}
