package org.consensusj.analytics.service;

import io.reactivex.rxjava3.core.Single;
import org.reactivestreams.Publisher;

/**
 * Interface for reactive rich list service
 *
 * @param <N> Numeric type for balances
 * @param <ID> Type for currency identifiers (e.g. String, Omni Currency ID, etc)
 */
public interface RichListService<N extends Number & Comparable<? super N>, ID> {

    // TODO: Convert Single to CompletableFuture
    /**
     * Return a single rich list
     *
     * @param currencyID The currency ID
     * @param numEntries The requested number of entries in the list
     * @return An RxJava Single for lazy access to the response
     */
    Single<TokenRichList<N, ID>> richList(ID currencyID, int numEntries);

    /**
     * Get a continuous stream of rich list updates
     *
     * @param currencyID The currency ID
     * @param numEntries The requested number of entries in each list
     * @return A Publisher for lazy access to the stream
     */
    Publisher<TokenRichList<N, ID>> richListUpdates(ID currencyID, int numEntries);
}
