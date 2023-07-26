package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.queue.TransactionRecordQueue;
import org.wso2.carbon.apimgt.gateway.handlers.transaction.store.TransactionRecordStore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRecordConsumer {

    // Todo: Make these parameters configurable via deployment.toml
    private int MAX_RETRY_COUNT = 3;
    private int TRANSACTION_COUNT_COMMIT_INTERVAL = 10;
    private int MAX_TRANSACTION_RECORDS_PER_COMMIT = 10;

    private static final Log LOG = LogFactory.getLog(TransactionRecordConsumer.class);
    private static TransactionRecordConsumer instance = null;
    private TransactionRecordStore transactionRecordStore;
    private TransactionRecordQueue transactionRecordQueue;
    private ExecutorService executorService;
    private final int threadPoolSize;

    private TransactionRecordConsumer(TransactionRecordStore transactionRecordStore,
                                      TransactionRecordQueue transactionRecordQueue, int threadPoolSize) {
        this.transactionRecordStore = transactionRecordStore;
        this.transactionRecordQueue = transactionRecordQueue;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.threadPoolSize = threadPoolSize;
    }

    public static TransactionRecordConsumer getInstance(TransactionRecordQueue transactionRecordQueue,
                                                        TransactionRecordStore transactionRecordStore,
                                                        int threadPoolSize) {
        if(instance == null) {
            instance = new TransactionRecordConsumer(transactionRecordStore, transactionRecordQueue, threadPoolSize);
        }
        return instance;
    }

    public void start() {
        LOG.info("Transaction record consumer started");
        // execute the startCommitting method in all the threads
        for (int i = 0; i < threadPoolSize; i++) {
            executorService.execute(this::startCommitting);
        }
    }

    private void startCommitting() {
        try {
            while (true) {
                commitWithRetries();
            }
        } catch (InterruptedException ex) {
            LOG.debug("Transaction record consumer interrupted");
        }
    }

    private void commitWithRetries() throws InterruptedException {

        // Arraylist of transaction count records will be committed to the store
        ArrayList<TransactionRecord> transactionRecordList = new ArrayList<>();
        TransactionRecord transactionRecord = null;
        transactionRecord = transactionRecordQueue.take();

        transactionRecordList.add(transactionRecord);
        transactionRecordQueue.drain(transactionRecordList, MAX_TRANSACTION_RECORDS_PER_COMMIT);

        // Committing the transaction count records to the store with retries
        // If failed to commit after MAX_RETRY_COUNT, the transaction count records will be added to the queue again
        boolean commited = false;
        int retryCount = 0;
        while (!commited && retryCount < MAX_RETRY_COUNT) {
            commited = this.transactionRecordStore.commit(transactionRecordList);
            retryCount++;
        }
        if (!commited) {
            transactionRecordQueue.addAll(transactionRecordList);
        }
    }

    public void shutdown() {
        this.executorService.shutdownNow();
    }

}
