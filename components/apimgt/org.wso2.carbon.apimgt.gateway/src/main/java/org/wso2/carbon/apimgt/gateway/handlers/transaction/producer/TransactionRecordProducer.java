package org.wso2.carbon.apimgt.gateway.handlers.transaction.producer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.config.TransactionCounterConfig;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.record.TransactionRecord;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.queue.TransactionRecordQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionRecordProducer {

    private static double MAX_TRANSACTION_COUNT;
    private static double MIN_TRANSACTION_COUNT;
    private static int TRANSACTION_COUNT_RECORD_INTERVAL;
    private static final Log LOG = LogFactory.getLog(TransactionRecordProducer.class);
    private static TransactionRecordProducer instance = null;
    private TransactionRecordQueue transactionRecordQueue;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private static ReentrantLock lock = new ReentrantLock();
    private static AtomicInteger transactionCount = new AtomicInteger(0);
    private TransactionRecordProducer(TransactionRecordQueue transactionRecordQueue, int threadPoolSize) {

        // Obtain config values
        MAX_TRANSACTION_COUNT = TransactionCounterConfig.getMaxTransactionCount();
        MIN_TRANSACTION_COUNT = TransactionCounterConfig.getMinTransactionCount();
        TRANSACTION_COUNT_RECORD_INTERVAL = TransactionCounterConfig.getTransactionCountRecordInterval();

        this.transactionRecordQueue = transactionRecordQueue;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static TransactionRecordProducer getInstance(TransactionRecordQueue transactionRecordQueue,
                                                        int threadPoolSize) {
        if(instance == null) {
            instance = new TransactionRecordProducer(transactionRecordQueue, threadPoolSize);
        }
        return instance;
    }

    public void start() {
        LOG.info("Transaction record producer started");
        // Start the transaction count record scheduler
        scheduledExecutorService.scheduleAtFixedRate(this::produceRecordScheduled,
                0, TRANSACTION_COUNT_RECORD_INTERVAL, TimeUnit.SECONDS);
    }

    public void addTransaction() {
        executorService.execute(this::produceRecord);
    }

    private void produceRecord() {
        lock.lock();
        try {
            int count = transactionCount.incrementAndGet();
            LOG.info("Transaction count is incremented to: " + count);
            if (count >= MAX_TRANSACTION_COUNT) {
                TransactionRecord transactionRecord = new TransactionRecord(transactionCount.get());
                LOG.info("Transaction count is added to the queue from producer");
                transactionRecordQueue.add(transactionRecord);
                transactionCount.set(0);
            }
        } catch (Exception e) {
            LOG.error("Error while handling transaction count.", e);
        } finally {
            lock.unlock();
        }
    }

    private void produceRecordScheduled() {
        lock.lock();
        try {
            int transactionCountValue = transactionCount.get();
            if (transactionCountValue >= MIN_TRANSACTION_COUNT) {
                TransactionRecord transactionRecord = new TransactionRecord(transactionCountValue);
                LOG.info("Transaction count is added to the queue from scheduled producer");
                transactionRecordQueue.add(transactionRecord);
                transactionCount.set(0);
            }
        } catch (Exception e) {
            LOG.error("Error while handling transaction count.", e);
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
        executorService.shutdownNow();
    }

}
