package org.consensusj.rx.jsonrpc;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.AsyncSupport;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * RxJava support for calling JSON-RPC clients. Extend/implement this interface to inherit {@code default} methods
 * {@link #call(ThrowingSupplier)}, {@link #defer(Supplier)}, and {@link #pollOnceAsPublisher(Supplier, DefaultRpcClient.TransientErrorFilter)}.
 */
public interface RxJsonRpcClient extends AsyncSupport {
    Logger log = LoggerFactory.getLogger(RxJsonRpcClient.class);

    /**
     * Return a <i>cold</i> {@link Single} for calling a provided <b>synchronous</b> JSON-RPC method.
     * <p>
     *  A  <i>cold</i> stream does not begin processing until someone subscribes to it.
     * @param method A {@link org.consensusj.jsonrpc.AsyncSupport.ThrowingSupplier} wrapper for a method call.
     * @param <RSLT> The type of the expected result
     * @return A <i>cold</i> {@link Single} for calling the method.
     */
    @Deprecated
    default <RSLT> Single<RSLT> call(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return Single.defer(() -> Single.fromCompletionStage(supplyAsync(method)));
    }

    @Deprecated
    default <RSLT> Single<RSLT> callAsync(Supplier<CompletionStage<RSLT>> supplier) {
        return defer(supplier);
    }

    /**
     * Return a <i>cold</i> {@link Single} for calling a provided <b>asynchronous</b> JSON-RPC method.
     * (Uses a supplier to make sure the async call isn't made until subscription time)
     * <p>
     *  A  <i>cold</i> stream does not begin processing until someone subscribes to it.
     * @param supplier of completable
     * @param <RSLT> The type of the expected result
     * @return A <i>cold</i> {@link Single} for calling the method.
     */
    static <RSLT> Single<RSLT> defer(Supplier<CompletionStage<RSLT>> supplier) {
        return Single.defer(() -> Single.fromCompletionStage(supplier.get()));
    }

    /**
     * Takes a supplier of computable futures and returns a publisher that when subscribed to returns 0 or 1 items.
     * @param supplier supplier for delaying invocation of "hot" futures so they can be "cold" publishers.
     * @param filter Filters and logs transient errors
     * @return A publisher of a "cold" stream of items (temporarily Flowable, but will change to Publisher, then Flow.Publisher)
     * @param <T> result type
     */
    default <T> Publisher<T> pollOnceAsPublisher(Supplier<CompletionStage<T>> supplier, DefaultRpcClient.TransientErrorFilter filter) {
        return Flowable.defer(() -> Flowable.fromCompletionStage(supplier.get()
                        .handle(filter::handle)
                        .thenCompose(Function.identity())))
                .flatMapStream(Optional::stream);
    }

    // This version doesn't filter or log any exceptions
    default <T> Publisher<T> pollOnceAsPublisher(Supplier<CompletableFuture<T>> supplier) {
        return Flowable.defer(() -> Flowable.fromCompletionStage(supplier.get()));
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
     * @deprecated Use {@link #pollOnceAsPublisher(Supplier, TransientErrorFilter)} (Supplier)}
     */
    @Deprecated
    default <RSLT> Maybe<RSLT> pollOnce(AsyncSupport.ThrowingSupplier<RSLT> method) {
        return call(method)
                .doOnSuccess(this::logSuccess)
                .doOnError(this::logError)
                .toMaybe()
                .onErrorComplete(this::isTransientError);    // Empty completion if IOError
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
     * @param supplier A supplier (should call an async RPC Method and return a {@code CompletableFuture}).
     * @param <RSLT> The type of the expected result
     * @return A Maybe for the expected result type
     */
    @Deprecated
    default <RSLT> Maybe<RSLT> pollOnceAsync(Supplier<CompletionStage<RSLT>> supplier) {
        return Flowable.fromPublisher(pollOnceAsPublisher(supplier, TransientErrorFilter.of(this::isTransientError, this::logError)))
                .firstElement();
    }


    /**
     * Determine if error is transient and should be ignored.
     * <p>
     * TODO: Ignoring all IOError is too broad
     * 
     * @param t Error thrown from calling an RPC method
     * @return true if the error is transient and can be ignored
     */
    private boolean isTransientError(Throwable t) {
        return t instanceof IOError;
    }
    
    private <RSLT> void logSuccess(RSLT result) {
        log.debug("RPC call returned: {}", result);
    }

    private void logError(Throwable throwable) {
        log.error("Exception in RPCCall", throwable);
    }
}
