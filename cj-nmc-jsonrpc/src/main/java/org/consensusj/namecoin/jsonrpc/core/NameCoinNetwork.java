package org.consensusj.namecoin.jsonrpc.core;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.base.Monetary;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.SegwitAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.bitcoinj.base.Coin.COIN;

/**
 *
 */
public enum NameCoinNetwork implements Network {
    MAINNET("org.namecoin.mainnet"),
    TESTNET("org.namecoin.testnet"),
    SIGNET("org.namecoin.signet"),
    REGTEST("org.namecoin.regtest");

    /**
     * Scheme part for Bitcoin URIs.
     */
    public static final String BITCOIN_SCHEME = "bitcoin";

    /**
     * The maximum number of coins to be generated
     */
    private static final long MAX_COINS = 21_000_000;

    /**
     * The maximum money to be generated
     */
    public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);

    /** The ID string for the main, production network where people trade things. */
    public static final String ID_MAINNET = MAINNET.id();
    /** The ID string for the testnet. */
    public static final String ID_TESTNET = TESTNET.id();
    /** The ID string for the signet. */
    public static final String ID_SIGNET = SIGNET.id();
    /** The ID string for regtest mode. */
    public static final String ID_REGTEST = REGTEST.id();
    /** The ID string for the Unit test network -- there is no corresponding {@code enum}. */
    public static final String ID_UNITTESTNET = "org.bitcoinj.unittest";

    private final String id;

    // All supported names for this BitcoinNetwork
    private final List<String> allNames;

    // Maps from names and alternateNames to BitcoinNetwork
    private static final Map<String, NameCoinNetwork> stringToEnum = mergedNameMap();

    NameCoinNetwork(String networkId, String... alternateNames) {
        this.id = networkId;
        this.allNames = combine(this.toString(), alternateNames);
    }

    /**
     * Return the canonical, lowercase, user-facing {@code String} for an {@code enum}
     * @return canonical lowercase value
     */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Return the network ID string (previously specified in {@code NetworkParameters})
     *
     * @return The network ID string
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Header byte of base58 encoded legacy P2PKH addresses for this network.
     * @return header byte as an {@code int}.
     * @see LegacyAddress.AddressHeader
     */
    public int legacyAddressHeader() {
        //return LegacyAddress.AddressHeader.ofNetwork(this).headerByte();
        return 52;
    }

    /**
     * Header byte of base58 encoded legacy P2SH addresses for this network.
     * @return header byte as an {@code int}.
     * @see LegacyAddress.P2SHHeader
     */
    public int legacyP2SHHeader() {
        //return LegacyAddress.P2SHHeader.ofNetwork(this).headerByte();
        return 5;
    }

    /**
     * Return the standard Bech32 {@link org.bitcoinj.base.SegwitAddress.SegwitHrp} (as a {@code String}) for
     * this network.
     * @return The HRP as a (lowercase) string.
     */
    public String segwitAddressHrp() {
        //return LegacyAddress.P2SHHeader.ofNetwork(this).headerByte();
        return "??";
    }

    /**
     * The URI scheme for Bitcoin.
     * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki">BIP 0021</a>
     * @return string containing the URI scheme
     */
    @Override
    public String uriScheme() {
        return BITCOIN_SCHEME;
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }

    @Override
    public Coin maxMoney() {
        return MAX_MONEY;
    }

    @Override
    public boolean exceedsMaxMoney(Monetary amount) {
        if (amount instanceof Coin) {
            return ((Coin) amount).compareTo(MAX_MONEY) > 0;
        } else {
            throw new IllegalArgumentException("amount must be a Coin type");
        }
    }

    /**
     * Find the {@code BitcoinNetwork} from a name string, e.g. "mainnet", "testnet" or "signet".
     * A number of common alternate names are allowed too, e.g. "main" or "prod".
     * @param nameString A name string
     * @return An {@code Optional} containing the matching enum or empty
     */
    public static Optional<NameCoinNetwork> fromString(String nameString) {
        return Optional.ofNullable(stringToEnum.get(nameString));
    }

    /**
     * Find the {@code BitcoinNetwork} from an ID String
     * <p>
     * Note: {@link #ID_UNITTESTNET} is not supported as an enum
     * @param idString specifies the network
     * @return An {@code Optional} containing the matching enum or empty
     */
    public static Optional<NameCoinNetwork> fromIdString(String idString) {
        return Arrays.stream(values())
                .filter(n -> n.id.equals(idString))
                .findFirst();
    }

    // Create a Map that maps name Strings to networks for all instances
    private static Map<String, NameCoinNetwork> mergedNameMap() {
        return Stream.of(values())
                .collect(HashMap::new,                  // Supply HashMaps as mutable containers
                        NameCoinNetwork::accumulateNames,    // Accumulate one network into hashmap
                        Map::putAll);                       // Combine two containers
    }

    // Add allNames for this Network as keys to a map that can be used to find it
    private static void accumulateNames(Map<String, NameCoinNetwork> map, NameCoinNetwork net) {
        net.allNames.forEach(name -> map.put(name, net));
    }

    // Combine a String and an array of String and return as an unmodifiable list
    private static List<String> combine(String canonical, String[] alternateNames) {
        List<String> temp = new ArrayList<>();
        temp.add(canonical);
        temp.addAll(Arrays.asList(alternateNames));
        return Collections.unmodifiableList(temp);
    }

}
