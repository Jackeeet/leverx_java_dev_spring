package com.pg.leverx.hw3;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class StoreThreads implements CommandLineRunner {
    private final Store store;
    private final ExecutorService executor;
    private final ObjectProvider<Customer> customerProvider;
    private final ObjectProvider<OrderWorker> orderWorkerProvider;

    public StoreThreads(
            Store store, ExecutorService executor,
            ObjectProvider<Customer> customerProvider, ObjectProvider<OrderWorker> orderWorkerProvider
    ) {
        this.store = store;
        this.executor = executor;
        this.customerProvider = customerProvider;
        this.orderWorkerProvider = orderWorkerProvider;
    }

    public static void main(String[] args) {
        SpringApplication.run(StoreThreads.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Main <number of customers> <number of workers> [max number of products] [max quantity] [debug]");
            System.exit(1);
        }

        try {
            int customerCount = Integer.parseInt(args[0]);
            int workerCount = Integer.parseInt(args[1]);

            if (customerCount <= 0 || workerCount <= 0) {
                System.err.println("Error: number of customers and number of workers must be positive integers.");
                System.exit(1);
            }

            int maxProducts = args.length > 2 ? parseIntOrDefault(args[2], 10) : 10;
            int maxQuantity = args.length > 3 ? parseIntOrDefault(args[3], 20) : 20;

            if (maxProducts <= 0 || maxQuantity <= 0) {
                System.err.println("Error: max number of products and max quantity must be positive integers.");
                System.exit(1);
            }

            boolean debug = false;
            if (args.length > 4) {
                debug = Boolean.parseBoolean(args[4]);
            }

            store.initializeWarehouse(maxProducts, maxQuantity);

            for (int i = 0; i < customerCount; i++) {
                executor.submit(customerProvider.getObject(i + 1, store));
            }
            for (int i = 0; i < workerCount; i++) {
                executor.submit(orderWorkerProvider.getObject(i + 1, store));
            }

            executor.shutdown();
            boolean _ = executor.awaitTermination(15, TimeUnit.SECONDS);

            if (debug) {
                System.out.println("----");
                store.printWarehouseInfo();
                System.out.println("----");
                store.printSoldProductsInfo();
                System.out.println("----");
            }

            System.out.println(store.getStoreAnalytics(3).getPrettyPrintString());
        } catch (NumberFormatException e) {
            System.err.println("Error: number of customers and number of workers must be positive integers.");
        }
    }

    private static int parseIntOrDefault(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer value " + s + ", using default value " + defaultValue);
            return defaultValue;
        }
    }
}
