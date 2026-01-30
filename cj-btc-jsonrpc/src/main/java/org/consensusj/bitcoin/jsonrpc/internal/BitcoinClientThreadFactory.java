/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.bitcoin.jsonrpc.internal;

import org.bitcoinj.core.Context;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;


/**
 * A {@link ThreadFactory} for the {@link BitcoinClient}. It is based upon <b>bitcoinj</b>'s
 * {@link org.bitcoinj.utils.ContextPropagatingThreadFactory} but differs in a few ways:
 * <ul>
 *     <li>It takes a {@link Context} in the constructor rather than propagating the Context from the caller. We want
 *     all threads in the pool to have the same context as the associated {@code BitcoinClient} and (more importantly)
 *      the connected server.</li>
 *     <li>Does not use <b>bitcoinj</b>'s {@link org.bitcoinj.utils.Threading#uncaughtExceptionHandler} mechanism.
 *     (For now, it just logs the uncaught exceptions.)</li>
 *     <li>Has no direct dependencies on Guava, though it does depend on <b>bitcoinj</b>.</li>
 *
 * </ul>
 */
public class BitcoinClientThreadFactory implements ThreadFactory {
    private static final Logger log = LoggerFactory.getLogger(BitcoinClientThreadFactory.class);
    private final Context context;
    private final String name;
    private final int priority;

    public BitcoinClientThreadFactory(Context context, String name, int priority) {
        this.context = context;
        this.name = name;
        this.priority = priority;
    }

    public BitcoinClientThreadFactory(Context context, String name) {
        this(context, name, Thread.NORM_PRIORITY);
    }

    @Override
    public Thread newThread(final Runnable r) {
        Thread thread = new Thread(() -> {
            try {
                Context.propagate(context);
                r.run();
            } catch (Throwable e) {
                log.error("Exception in thread", e);
                throw e;
            }
        }, name);
        thread.setPriority(priority);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(BitcoinClientThreadFactory::uncaughtException);
        return thread;
    }

    private static void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception in thread: ", e);
    }
}
