package org.consensusj.daemon.micronaut;

import org.consensusj.bitcoin.services.WalletAppKitService;
import org.consensusj.jsonrpc.introspection.DelegatingJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * JSON-RPC wrapper for WalletAppKitService
 */
@Singleton
public class WalletAppKitJsonRpcService extends DelegatingJsonRpcService {
    private static final Logger log = LoggerFactory.getLogger(WalletAppKitJsonRpcService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(WalletAppKitService.class);

    public WalletAppKitJsonRpcService(WalletAppKitService service) {
        super(methods, service);
        log.info("Constructing WalletAppKitJsonRpcService");
    }
}
