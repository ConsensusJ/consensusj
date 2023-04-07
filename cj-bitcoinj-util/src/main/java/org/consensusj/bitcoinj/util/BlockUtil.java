package org.consensusj.bitcoinj.util;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.base.internal.ByteUtils;

import java.math.BigInteger;
import java.util.Arrays;

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
        // There appears to be a bug in bitcoinj 0.15.8 for recent blocks where the input[0] script of the coinbase tx can't be parsed
        // The below code works for block 324140 but seems to fail on SegWit blocks, due to https://github.com/bitcoinj/bitcoinj/issues/1595
//        Script script = new Script(scriptBytes);
//        ScriptChunk chunk = script.getChunks().get(0);
//        BigInteger height = new BigInteger(Utils.reverseBytes(chunk.data));
        // Workaround: We'll parse just the bytes we need
        BigInteger height;
        if (scriptBytes[0] == 3) {
            byte[] blockNumBytes = Arrays.copyOfRange(scriptBytes, 1, 4);
            height = new BigInteger(ByteUtils.reverseBytes(blockNumBytes));
        } else {
            height = BigInteger.valueOf(-1);
        }
        return height.intValueExact();
    }
}
