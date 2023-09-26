package org.consensusj.jsonrpc;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
     * @param throwingSupplier Supplier of T that can throw an {@code IOException}
     * @param <T> return type
     * @return A completable future, returning T
     */
    default <T> CompletableFuture<T> supplyAsync(ThrowingSupplier<T> throwingSupplier) {
        return supplyAsync(throwingSupplier, getDefaultAsyncExecutor());
    }

    /**
     * Supply async for a ThrowingSupplier by catching exceptions and completing exceptionally.
     *
     * @param throwingSupplier Supplier of T that can throw an {@code IOException}
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

    private static <T> CompletableFuture<T> supplyCatchingAsync(ThrowingSupplier<T> throwingSupplier, Executor executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                T result = throwingSupplier.getThrows();
                future.complete(result);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Subinterface of {@link Supplier} for Lambdas which throw {@link IOException}.
     * Can be used for two purposes:
     * <ol>
     *     <li>To cast a lambda that throws an {@code IOException} to a {@link Supplier} while
     *      automatically wrapping any exception thrown by the lambda with {@link RuntimeException}.</li>
     *     <li>As a {@code FunctionalInterface} where a lambda that throws exceptions is
     *      expected or allowed.</li>
     * </ol>
     * This is intended to be used to wrap JSON-RPC I/O methods of {@link JsonRpcClient} that throw {@link IOException} and
     * subclasses such as {@link JsonRpcException}, so we have narrowed the allowed exceptions in {@link #getThrows()} to
     * {@link IOException}.
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
         * @throws IOException A (checked) exception
         */
        T getThrows() throws IOException;
    }

    /**
     * Error filter for resilient polling. Uses a Predicate to specify what to ignore and a Consumer to log
     * what is ignored.
     */
    interface TransientErrorFilter {
        static TransientErrorFilter of(Predicate<Throwable> filter, Consumer<Throwable> logger) {
            return new TransientErrorFilter() {
                @Override
                public boolean isTransient(Throwable t) {
                    return filter.test(t);
                }

                @Override
                public void log(Throwable t) {
                    logger.accept(t);
                }
            };
        }

        static TransientErrorFilter none() {
            return of(
                    (t) -> false,   // No errors are consider transient
                    (t) -> {}       // Nothing to log because we're not swallowing anything.
            );
        }

        /**
         * Handler to transpose to a "future maybe". Use with {@link CompletableFuture#handle(BiFunction)}
         * followed by {@code .thenCompose(Function.identity())} (or if JDK 12+ {@link CompletableFuture#exceptionallyCompose(Function)})
         * to swallow transient errors.
         * @param result T
         * @param t An error, possibly transient
         * @return A completable future of future maybe
         * @param <T> The desired return type
         */
        default <T> CompletableFuture<Optional<T>> handle(T result, Throwable t) {
            if (result != null) {
                return CompletableFuture.completedFuture(Optional.of(result));
            } else if (isTransient(t)) {
                log(t);
                return CompletableFuture.completedFuture(Optional.empty());
            } else {
                return CompletableFuture.failedFuture(t);
            }
        }

        // TODO: Should this be flipped to isFatal()?
        boolean isTransient(Throwable t);
        void log(Throwable t);

    }
}
