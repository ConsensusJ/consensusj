package org.consensusj.daemon.micronaut;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import io.micronaut.context.annotation.Context;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Embryonic implementation of Bitcoin Core REST API. Incomplete and incorrect at ths point.
 */
@Controller("/rest")
@Context
public class BitcoinRestController {
    private static final Logger log = LoggerFactory.getLogger(BitcoinRestController.class);
    private final WalletAppKitService walletAppKitService;

    public BitcoinRestController(WalletAppKitService walletAppKitService) {
        log.info("Constructing JsonRpcController");
        this.walletAppKitService = walletAppKitService;
    }

    @Get(uri="/block/{hash}.json", produces = MediaType.APPLICATION_JSON)
    public BlockInfo block(@PathVariable String hash) {
        log.info("chaininfo REST GET call");
        return walletAppKitService.getBlockInfo(Sha256Hash.wrap(hash), BlockInfo.IncludeTxFlag.YES);
    }

    @Get(uri="/block/notxdetails/{hash}.json", produces = MediaType.APPLICATION_JSON)
    public BlockInfo blockNoTxDetails(@PathVariable String hash) {
        log.info("chaininfo REST GET call");
        return walletAppKitService.getBlockInfo(Sha256Hash.wrap(hash),  BlockInfo.IncludeTxFlag.IDONLY);
    }

    @Get("/chaininfo.json")
    public BlockChainInfo chaininfo() {
        log.info("chaininfo REST GET call");
        return walletAppKitService.getblockchaininfo();
    }

}
