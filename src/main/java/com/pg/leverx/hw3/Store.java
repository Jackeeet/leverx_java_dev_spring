package com.pg.leverx.hw3;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class Store {
    private static final Random RANDOM_GEN = new Random();
    private static final double MIN_PRICE = 0.01;
    private static final double MAX_PRICE = 1000.00;

    public final ConcurrentMap<Product, StockQuantity> warehouse;
    public final CopyOnWriteArrayList<Order> processedOrders;
    public final BlockingQueue<Order> queuedOrders;

    private boolean warehouseInitialized = false;

    public Store() {
        this.warehouse = new ConcurrentHashMap<>();
        this.processedOrders = new CopyOnWriteArrayList<>();
        this.queuedOrders = new LinkedBlockingQueue<>();
    }

    // product catalog: all products from the warehouse where quantity is not zero
    public List<Map.Entry<Product, Integer>> getProductCatalog() {
        return this.warehouse.entrySet().parallelStream()
                .filter(productEntry -> productEntry.getValue().available > 0)
                .map(productEntry ->
                        Map.entry(productEntry.getKey(), productEntry.getValue().available)
                )
                .toList();
    }

    public boolean placeReservation(Reservation reservation) {
        AtomicBoolean success = new AtomicBoolean(false);
        this.warehouse.computeIfPresent(reservation.product, (_, qtyInStock) -> {
            if (qtyInStock.available < reservation.quantity) {
                return qtyInStock;
            }

            success.set(true);
            reservation.setTotalStockAtReserveTime(qtyInStock.total());
            return new StockQuantity(
                    qtyInStock.available - reservation.quantity,
                    qtyInStock.reserved + reservation.quantity
            );
        });

        return success.get();
    }

    public void cancelReservation(Reservation reservation) {
        this.warehouse.compute(reservation.product, (_, qtyInStock) -> new StockQuantity(
                qtyInStock.available + reservation.quantity, qtyInStock.reserved - reservation.quantity)
        );
    }

    // method for debug purposes
    public void printWarehouseInfo() {
        System.out.println("Current warehouse status:");
        List<Map.Entry<Product, StockQuantity>> entries = this.warehouse.entrySet().parallelStream()
                .sorted(Comparator.comparingInt(e -> e.getKey().productId))
                .toList();
        for (Map.Entry<Product, StockQuantity> entry : entries) {
            System.out.println(entry.getKey() + " (" + entry.getValue() + ")");
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
            System.out.println(soldProduct.getKey() + " (" + soldProduct.getValue() + ", " + productProfit + ")");
        }
    }

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
            warehouse.put(product, new StockQuantity(quantity, 0));
            System.out.println(product + " (x" + quantity + ")");
        }
        warehouseInitialized = true;
    }
}
