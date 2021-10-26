package org.consensusj.bitcoin.future;

import java.util.concurrent.CompletableFuture;

/**
 * A {@link CompletableFuture} that is also a {@link com.google.common.util.concurrent.ListenableFuture} for migration
 * from Guava {@code ListenableFuture} to {@link java.util.concurrent.CompletableFuture}.
 */
public class ListenableCompletableFuture<V> extends CompletableFuture<V> implements ListenableCompletionStage<V> {
}
