package org.consensusj.bitcoin.zeromq;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.consensusj.jsonrpc.AsyncSupport;
import org.consensusj.jsonrpc.JsonRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;

/**
 * RxJava methods for calling JSON-RPC clients
 */
public interface RxJsonRpcClient extends JsonRpcClient {
    Logger log = LoggerFactory.getLogger(RxBitcoinJsonRpcClient.class);

    /**
     * Poll a method, ignoring {@link IOError}.
     * The returned {@link Maybe} will:
     * <ol>
     *     <li>Emit a value if successful</li>
     *     <li>Empty Complete on IOError</li>
     *     <li>Error out if any other Exception occurs</li>
     * </ol>
     *
     * @param method A supplier (should be an RPC Method) that can throw {@link Exception}.
     * @param <RSLT> The type of the expected result
     * @return A Maybe for the expected result type
     */
    default <RSLT> Maybe<RSLT> pollOnce(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return call(method)
                .doOnSuccess(this::logSuccess)
                .doOnError(this::logError)
                .toMaybe()
                .onErrorComplete(this::isTransientError);    // Empty completion if IOError
    }

    /**
     * Determine if error is transient and should be ignored
     *
     * TODO: Ignoring all IOError is too broad
     * 
     * @param t Error thrown from calling an RPC method
     * @return true if the error is transient and can be ignored
     */
    default boolean isTransientError(Throwable t) {
        return t instanceof IOError;
    }

    default <RSLT> Single<RSLT> call(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Single.defer(() -> Single.fromCompletionStage(this.supplyAsync(method)));
    }

    private <RSLT> void logSuccess(RSLT result) {
        log.debug("RPC call returned: {}", result);
    }

    private void logError(Throwable throwable) {
        log.error("Exception in RPCCall", throwable);
    }
}
