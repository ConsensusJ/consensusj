package org.consensusj.rx.jsonrpc;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.consensusj.jsonrpc.AsyncSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * RxJava support for calling JSON-RPC clients.
 */
public interface RxJsonRpcClient extends AsyncSupport {
    Logger log = LoggerFactory.getLogger(RxJsonRpcClient.class);

    /**
     * Return a "hot" {@link Single} for calling a provided synchronous JSON-RPC method.
     *
     * @param method A {@link org.consensusj.jsonrpc.AsyncSupport.ThrowingSupplier} wrapper for a method call.
     * @param <RSLT> The type of the expected result
     * @return A "hot" {@link Single} for calling the method.
     */
    default <RSLT> Single<RSLT> call(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Single.defer(() -> Single.fromCompletionStage(supplyAsync(method)));
    }

    /**
     * Return a "hot" {@link Single} for calling a provided asynchronous JSON-RPC method.
     * (Uses a supplier to make sure the async call isn't made until subscription time)
     *
     * @param supplier of completable
     * @param <RSLT>
     * @return
     */
    default <RSLT> Single<RSLT> defer(Supplier<CompletionStage<RSLT>> supplier) {
        return Single.defer(() -> Single.fromCompletionStage(supplier.get()));
    }

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
    
    private <RSLT> void logSuccess(RSLT result) {
        log.debug("RPC call returned: {}", result);
    }

    private void logError(Throwable throwable) {
        log.error("Exception in RPCCall", throwable);
    }
}
