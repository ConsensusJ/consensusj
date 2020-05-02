package org.consensusj.exchange;

import org.consensusj.exchange.knowm.KnowmExchangeRateProvider;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.money.CurrencyUnit;
import javax.money.convert.ExchangeRateProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 *  Base ExchangeRateProvider using XChange library
 *  Currently supports current DEFERRED rates only
 */
@Deprecated
public abstract class BaseXChangeExchangeRateProvider extends KnowmExchangeRateProvider implements ExchangeRateProvider, ObservableExchangeRateProvider {
    private static final Logger log = LoggerFactory.getLogger(BaseXChangeExchangeRateProvider.class);
    private static final int initialDelay = 0;
    private static final int period = 60;

    private final ScheduledExecutorService stpe;
    private volatile boolean started = false;
    private volatile boolean stopping = false;
    private ScheduledFuture<?> future;

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClassName Classname of XChange exchange we are wrapping
     * @param scheduledExecutorService Executor service for running polling task
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(String exchangeClassName,
                                              ScheduledExecutorService scheduledExecutorService,
                                              Map<CurrencyUnit, String> tickerSymbolConversions,
                                              Collection<CurrencyUnitPair> pairs) {
        super(exchangeClassName, tickerSymbolConversions, pairs);
        stpe = (scheduledExecutorService != null) ? scheduledExecutorService : Executors.newScheduledThreadPool(1);
    }

    protected BaseXChangeExchangeRateProvider(String exchangeClassName,
                                              ScheduledExecutorService scheduledExecutorService,
                                              Map<CurrencyUnit, String> tickerSymbolConversions,
                                              CurrencyUnitPair... pairs) {
        this(exchangeClassName, scheduledExecutorService, tickerSymbolConversions, Arrays.asList(pairs));
    }

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClass Class of XChange exchange we are wrapping
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              CurrencyUnitPair... pairs) {
        this(exchangeClass.getName(),
                null,
                null,
                Arrays.asList(pairs));
    }

    /**
     * Construct using an XChange Exchange class object for a set of currencies
     * @param exchangeClass Class of XChange exchange we are wrapping
     * @param scheduledExecutorService Executor service for running polling task
     * @param pairs pairs to monitor
     */
    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              ScheduledExecutorService scheduledExecutorService,
                                              CurrencyUnitPair... pairs) {
        this(exchangeClass.getName(), scheduledExecutorService, null, Arrays.asList(pairs));
    }

    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              ScheduledExecutorService scheduledExecutorService,
                                              String... pairs) {
        this(exchangeClass.getName(), scheduledExecutorService, null, ExchangeUtils.pairsConvert(pairs));
    }

    protected BaseXChangeExchangeRateProvider(Class<? extends Exchange> exchangeClass,
                                              String... pairs) {
        this(exchangeClass.getName(), null, null, ExchangeUtils.pairsConvert(pairs));
    }

    protected BaseXChangeExchangeRateProvider(String exchangeClassName, ScheduledExecutorService scheduledExecutorService, String[] pairs) {
        this(exchangeClassName, scheduledExecutorService, null, ExchangeUtils.pairsConvert(pairs));
    }
    


    /**
     * Initialize the exchange provider and start polling thread
     */
    @Override
    public synchronized void start()  {
        if (!started) {
            initialize();
            future = stpe.scheduleWithFixedDelay(this::poll, initialDelay, period, TimeUnit.SECONDS);
        }
    }

    /**
     * stop the polling thread
     */
    @Override
    public synchronized void stop() {
        if (started && !stopping) {
            stopping = true;
            final ScheduledFuture<?> handle = future;
            Runnable task = () -> handle.cancel(true);
            stpe.schedule(task, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Poll the exchange for updated Tickers
     */
    protected void poll() {
        monitoredCurrencies.forEach((key, monitor) -> {
            try {
                monitor.setTicker(marketDataService.getTicker(monitor.exchangePair));
            } catch (IOException e) {
                // TODO: Exceptions should not be swallowed here (or at least not all exceptions)
                // Some IOExceptions may warrant retries, but not all of them
                // log and ignore IOException (we'll try polling again next interval)
                // Actually I'm seeing that the CoinMarketCap ticker is returning IOException
                // when it should return NotAvailableFromExchangeException
                log.error("IOException in BaseXChangeExchangeRateProvider::poll: {}", e);
            }
            notifyExchangeRateObservers(monitor);
        });
    }

    @Override
    public void registerExchangeRateObserver(CurrencyUnitPair pair, ExchangeRateObserver observer) {
        // TODO: validate rate as one this provider supports
        MonitoredCurrency monitor = monitoredCurrencies.get(pair);
        monitor.observerList.add(observer);
        // If we've got data already, call observer immediately
        if (monitor.isTickerAvailable()) {
            notifyObserver(observer, pair, monitor);
        }
    }

    private void notifyExchangeRateObservers(MonitoredCurrency monitor) {
        monitor.observerList.forEach(observer -> notifyObserver(observer, monitor.pair, monitor));
    }

    private void notifyObserver(ExchangeRateObserver observer, CurrencyUnitPair pair, MonitoredCurrency monitor) {
        try {
            observer.onExchangeRateChange(buildExchangeRateChange(pair, monitor.getTicker()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interruption flag just in case
        } catch (TimeoutException e) {
            throw new RuntimeException(e);      // Unlikely to happen since ticker has usually been fetched
        }
    }

}
