package org.consensusj.bitcoin.rx.peergroup;

import com.google.common.util.concurrent.ListenableFuture;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener;
import org.consensusj.bitcoin.rx.RxBlockchainService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provide RxJava Observables for {@link PeerGroup} information.
 * This implementation will work with a PeerGroup w/o a BlockStore.
 */
public class RxPeerGroup implements RxBlockchainService {
    private static final Logger log = LoggerFactory.getLogger(RxPeerGroup.class);
    private final PeerGroup peerGroup;
    private static final int initialDelay = 0;
    private static final int period = 1;
    
    private ScheduledExecutorService stpe;
    private ScheduledFuture<?> future;

    private final PublishProcessor<Transaction> transactionProcessor;
    private final PublishProcessor<Integer> blockHeightProcessor;

    public RxPeerGroup(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
        this.transactionProcessor = PublishProcessor.create();
        this.blockHeightProcessor = PublishProcessor.create();
        //start();   // TODO: See start()
    }

    @Override
    public NetworkParameters getNetworkParameters() {
        return peerGroup.getVersionMessage().getParams();
    }

    /**
     * TODO: Fix this. RxPeerGroup implements BlockchainService which does not have a {@code start()} method.
     * But if we move (@code start()} to the constructor, it breaks PeerWatcher.
     */
    public void start() {
        ListenableFuture<?> groupStartedFuture = peerGroup.startAsync();

        // This should be a BehaviorSubject or something that caches the last message
        blockHeightProcessor.onNext(peerGroup.getMostCommonChainHeight());

        stpe = Executors.newScheduledThreadPool(2);
        future = stpe.scheduleAtFixedRate(this::updateBlockHeight, initialDelay, period, TimeUnit.SECONDS);

        groupStartedFuture.addListener(RxPeerGroup::peerGroupStartedListener, stpe);

        peerGroup.addConnectedEventListener(this::onPeerConnected);
        peerGroup.addDisconnectedEventListener(this::onPeerDisconnected);
        peerGroup.addOnTransactionBroadcastListener(this::onTransaction);
    }

    @Override
    public void close() {
        if (stpe != null) {
            if (future != null) {
                final ScheduledFuture<?> handle = future;
                Runnable task = () -> handle.cancel(true);
                stpe.schedule(task, 0, TimeUnit.SECONDS);
            }
            stpe.shutdown();
        }
        if (peerGroup != null && peerGroup.isRunning()) {
            peerGroup.stopAsync();
        }
    }

    @Override
    public Publisher<Transaction> transactionPublisher() {
        return transactionProcessor;
    }

    @Override
    public Publisher<Sha256Hash> transactionHashPublisher() {
        return transactionProcessor.map(Transaction::getTxId);
    }

    /**
     * TBD: Can {@link BlocksDownloadedEventListener} be used here?
     */
    @Override
    public Publisher<Block> blockPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * TBD: Can {@link BlocksDownloadedEventListener} be used here?
     */
    @Override
    public Publisher<Sha256Hash> blockHashPublisher() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Publisher<Integer> blockHeightPublisher() {
        return blockHeightProcessor;
    }

    private static void peerGroupStartedListener() {
        log.info("PeerGroup started listener called");
    }

    private int lastHeight = -1;
    private void updateBlockHeight() {
        Integer newHeight = getBlockHeight();
        if (newHeight != lastHeight) {
            lastHeight = newHeight;
            blockHeightProcessor.onNext(newHeight);
        }
    }

    private Integer getBlockHeight() {
        return peerGroup.getMostCommonChainHeight();
    }

    private void onTransaction(Peer peer, Transaction tx) {
        log.info("PeerWatcher: Got transaction: {}", tx.getTxId());
        transactionProcessor.onNext(tx);
    }

    private void onPeerConnected(Peer peer, int peerCount) {
        log.info("PeerWatcher: Peer Connected: {}, count: {}", peer.getAddress(), peerCount);
    }

    private void onPeerDisconnected(Peer peer, int peerCount) {
        log.info("PeerWatcher: Peer Disconnected, count: {}", peerCount);
    }

}
