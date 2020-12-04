package org.consensusj.bitcoin.rx;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import java.math.BigInteger;

/**
 * Utility class containing {@link #blockHeightFromCoinbase(Block)} 
 */
public class BlockUtil {

    /**
     * Return the block height contained in a standalone BIP34-compatible
     * Bitcoin block (or -1 if it's a pre-BIP34 block)
     * 
     * @param block A Bitcoin block (need not be attached to a chain)
     * @return The block height if available (BIP 34) or -1.
     */
    public static int blockHeightFromCoinbase(Block block) {
        if (!block.isBIP34()) {
            // Must be BIP34-compatible (block  227,836 or higher)
            return Block.BLOCK_HEIGHT_UNKNOWN;
        }
        Transaction coinbase = block.getTransactions().get(0);
        byte[] scriptBytes = coinbase.getInput(0).getScriptBytes();
        ScriptChunk chunk = new Script(scriptBytes).getChunks().get(0);
        BigInteger height = new BigInteger(Utils.reverseBytes(chunk.data));
        return height.intValueExact();
    }
}
