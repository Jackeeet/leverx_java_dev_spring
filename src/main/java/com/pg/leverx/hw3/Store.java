package com.pg.leverx.hw3;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class Store {
    private static final Random RANDOM_GEN = new Random();
    private static final double MIN_PRICE = 0.01;
    private static final double MAX_PRICE = 1000.00;

    public final ConcurrentMap<Product, Integer> warehouse;
    public final CopyOnWriteArrayList<Order> processedOrders;
    public final BlockingQueue<Order> queuedOrders;

    private boolean warehouseInitialized = false;

    public Store() {
//        this.warehouse = initializeWarehouse(maxProductCount, maxQuantity);

        // because maxProductCount and maxQuantity are provided by the user through the console,
        // after Spring initializes the Store singleton, I've changed the warehouse initialization
        // strategy to creating the HashMap instance in the constructor and initializing the data
        // later by passing user parameters through a method
        this.warehouse = new ConcurrentHashMap<>();
        this.processedOrders = new CopyOnWriteArrayList<>();
        this.queuedOrders = new LinkedBlockingQueue<>();
    }

    // product catalog: all products from the warehouse where quantity is not zero
    public List<Map.Entry<Product, Integer>> getProductCatalog() {
        return this.warehouse.entrySet().parallelStream()
                .filter(productEntry -> productEntry.getValue() > 0)
                .toList();
    }

    public Analytics getStoreAnalytics(int bestsellersCount) {
        int processedCount = Math.toIntExact(this.processedOrders.parallelStream()
                .filter(o -> o.getStatus() != Order.OrderStatus.NOT_PROCESSED)
                .count());
        int fulfilledCount = Math.toIntExact(this.processedOrders.parallelStream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PROCESSED_FULFILLED)
                .count());
        BigDecimal totalProfit = this.processedOrders.parallelStream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PROCESSED_FULFILLED)
                .map(o -> o.product.price.multiply(BigDecimal.valueOf(o.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Map.Entry<Product, Integer>> bestsellers = this.processedOrders.parallelStream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PROCESSED_FULFILLED)
                .map(o -> new AbstractMap.SimpleEntry<>(o.product, o.quantity))
                .collect(Collectors.groupingByConcurrent(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue))).entrySet().parallelStream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(bestsellersCount)
                .toList();

        return new Analytics(processedCount, fulfilledCount, totalProfit, bestsellers);
    }

    // method for debug purposes
    public void printWarehouseInfo() {
        System.out.println("Current warehouse status:");
        List<Map.Entry<Product, Integer>> entries = this.warehouse.entrySet().parallelStream()
                .sorted(Comparator.comparingInt(e -> e.getKey().productId))
                .toList();
        for (Map.Entry<Product, Integer> entry : entries) {
            System.out.println(entry.getKey() + " (x" + entry.getValue() + ")");
        }
    }

    // method for debug purposes
    public void printSoldProductsInfo() {
        System.out.println("Sold products info:");
        List<Map.Entry<Product, Integer>> soldProducts = this.processedOrders.parallelStream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PROCESSED_FULFILLED)
                .map(o -> new AbstractMap.SimpleEntry<>(o.product, o.quantity))
                .collect(Collectors.groupingByConcurrent(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue))).entrySet().parallelStream()
                .sorted(Comparator.comparingInt(e -> e.getKey().productId))
                .toList();
        for (Map.Entry<Product, Integer> soldProduct : soldProducts) {
            BigDecimal productProfit = soldProduct.getKey().price.multiply(BigDecimal.valueOf(soldProduct.getValue()));
            System.out.println(soldProduct.getKey() + " (x" + soldProduct.getValue() + ", " + productProfit + ")");
        }
    }

    // updated warehouse initialization method to use with Spring
    public void initializeWarehouse(int maxProductCount, int maxQuantity) {
        if (warehouseInitialized)
            return;

        int productIdCount = RANDOM_GEN.nextInt(1, maxProductCount + 1);
        System.out.println("There are " + productIdCount + " products in the warehouse:");
        for (int i = 0; i < productIdCount; i++) {
            BigDecimal price = BigDecimal.valueOf(RANDOM_GEN.nextDouble(MIN_PRICE, MAX_PRICE)).setScale(2, RoundingMode.HALF_UP);
            Product product = new Product(i + 1, price);
            int quantity = RANDOM_GEN.nextInt(1, maxQuantity + 1);
            // this is run once in a single thread, before other threads start modifying the data, so we can use 'put' safely
            warehouse.put(product, quantity);
            System.out.println(product + " (x" + quantity + ")");
        }
        warehouseInitialized = true;
    }

    // original warehouse initialization method to be called in the constructor

//    private static ConcurrentMap<Product, Integer> initializeWarehouse(int maxProductCount, int maxQuantity) {
//        ConcurrentMap<Product, Integer> warehouse = new ConcurrentHashMap<>();
//        int productIdCount = RANDOM_GEN.nextInt(1, maxProductCount + 1);
//        System.out.println("There are " + productIdCount + " products in the warehouse:");
//        for (int i = 0; i < productIdCount; i++) {
//            BigDecimal price = BigDecimal.valueOf(RANDOM_GEN.nextDouble(MIN_PRICE, MAX_PRICE)).setScale(2, RoundingMode.HALF_UP);
//            Product product = new Product(i + 1, price);
//            int quantity = RANDOM_GEN.nextInt(1, maxQuantity + 1);
//            // this code is run once at program startup in a single thread, so we can use 'put' safely
//            warehouse.put(product, quantity);
//            System.out.println(product + " (x" + quantity + ")");
//        }
//        return warehouse;
//    }

    public static class Analytics {
        // since the requirements don't specify whether an order has to be fulfilled successfully
        // in order to be counted in the total number of orders, I've decided to keep track of
        // all processed orders as well as of the successful orders
        public int processedOrderCount;
        public int fulfilledOrderCount;
        public BigDecimal totalProfit;
        public List<Map.Entry<Product, Integer>> bestsellers;

        public Analytics(
                int processedOrderCount, int fulfilledOrderCount,
                BigDecimal totalProfit, List<Map.Entry<Product, Integer>> bestsellers
        ) {
            this.processedOrderCount = processedOrderCount;
            this.fulfilledOrderCount = fulfilledOrderCount;
            this.totalProfit = totalProfit;
            this.bestsellers = bestsellers;
        }

        public String getPrettyPrintString() {
            StringBuilder message = new StringBuilder("Store analytics:")
                    .append("\n- orders processed: ").append(this.processedOrderCount)
                    .append("\n- orders fulfilled successfully: ").append(this.fulfilledOrderCount)
                    .append("\n- total profits: ").append(this.totalProfit)
                    .append("\n- top ").append(this.bestsellers.size()).append(" bestsellers: ");
            for (int i = 0; i < this.bestsellers.size(); i++) {
                Map.Entry<Product, Integer> bestseller = this.bestsellers.get(i);
                message.append("\n  ").append(i + 1)
                        .append(") Product ").append(bestseller.getKey().productId)
                        .append(", ").append(bestseller.getValue()).append(" items sold");
            }

            return message.toString();
        }

        @Override
        public String toString() {
            return "Analytics{" +
                    "processedOrderCount=" + processedOrderCount +
                    ", fulfilledOrderCount=" + fulfilledOrderCount +
                    ", totalProfit=" + totalProfit +
                    ", bestsellers=" + bestsellers +
                    '}';
        }
    }
}
