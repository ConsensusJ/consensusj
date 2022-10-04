package org.consensusj.jsonrpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Helper methods for creating asynchronous calls using {@link CompletableFuture}. Since the
 * synchronous methods in {@link AbstractRpcClient} throw checked exceptions this interface
 * provides wrapper support to make it easier to convert them to async calls.
 */
public interface AsyncSupport {
    /**
     * Supply async for a ThrowingSupplier by catching exceptions and completing exceptionally.
     *
     * @param throwingSupplier Supplier of T that can throw an exception
     * @param <T> return type
     * @return A completable future, returning T
     */
    default <T> CompletableFuture<T> supplyAsync(ThrowingSupplier<T> throwingSupplier) {
        return supplyAsync(throwingSupplier, getDefaultAsyncExecutor());
    }

    /**
     * Supply async for a ThrowingSupplier by catching exceptions and completing exceptionally.
     *
     * @param throwingSupplier Supplier of T that can throw an exception
     * @param executor Executor to run the Supplier
     * @param <T> return type
     * @return A completable future, returning T
     */
    default <T> CompletableFuture<T> supplyAsync(ThrowingSupplier<T> throwingSupplier, Executor executor) {
        return AsyncSupport.supplyCatchingAsync(throwingSupplier, executor);
    }

    /**
     * Return the default executor for supplying asynchronicity.
     *
     * @return An executor.
     */
    default Executor getDefaultAsyncExecutor() {
        return (Runnable r) -> new Thread(r).start();
    }

    static <T> CompletableFuture<T> supplyCatchingAsync(ThrowingSupplier<T> throwingSupplier, Executor executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                T result = throwingSupplier.getThrows();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Subinterface of {@link Supplier} for Lambdas which throw exceptions.
     * Can be used for two purposes:
     * 1. To cast a lambda that throws an exception to a {@link Supplier} and
     * automatically wrapping any exceptions with {@link RuntimeException}.
     * 2. As a {@code FunctionalInterface} where a lambda that throws exceptions is
     * expected or allowed.
     *
     * @param <T>
     */
    @FunctionalInterface
    interface ThrowingSupplier<T> extends Supplier<T> {

        /**
         * Gets a result wrapping checked Exceptions with {@link RuntimeException}
         * @return a result
         */
        @Override
        default T get() {
            try {
                return getThrows();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Gets a result.
         *
         * @return a result
         * @throws Exception Any checked Exception
         */
        T getThrows() throws Exception;
    }
}
