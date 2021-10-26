package org.consensusj.bitcoin.future;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * A {@link CompletionStage} with a {@link ListenableFuture}-compatible interface to smooth migration
 * from Guava {@code ListenableFuture} to {@link java.util.concurrent.CompletableFuture}/{@code CompletionStage}.
 * <p>
 * Note that this is much easier to implement than trying to extend {@link com.google.common.util.concurrent.AbstractFuture}
 * to implement {@code CompletionStage}.
 */
public interface ListenableCompletionStage<V> extends CompletionStage<V>, ListenableFuture<V> {
    @Override
    default void addListener(Runnable listener, Executor executor) {
        this.thenRunAsync(listener, executor);
    }
}
