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
}
