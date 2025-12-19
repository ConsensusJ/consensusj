package org.consensusj.daemon.micronaut;

import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.BlockInfo;
import io.micronaut.context.annotation.Context;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import org.bitcoinj.base.Sha256Hash;
import org.consensusj.bitcoin.services.WalletAppKitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;


/**
 * Embryonic implementation of Bitcoin Core REST API. Incomplete and incorrect at this point.
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
    public CompletableFuture<BlockInfo> block(@PathVariable String hash) {
        log.info("chaininfo REST GET call");
        return walletAppKitService.getBlockInfo(Sha256Hash.wrap(hash), BlockInfo.IncludeTxFlag.YES);
    }

    @Get(uri="/block/notxdetails/{hash}.json", produces = MediaType.APPLICATION_JSON)
    public CompletableFuture<BlockInfo> blockNoTxDetails(@PathVariable String hash) {
        log.info("chaininfo REST GET call");
        return walletAppKitService.getBlockInfo(Sha256Hash.wrap(hash),  BlockInfo.IncludeTxFlag.IDONLY);
    }

    @Get("/chaininfo.json")
    public CompletableFuture<BlockChainInfo> chaininfo() {
        log.info("chaininfo REST GET call");
        return walletAppKitService.getblockchaininfo();
    }

}
