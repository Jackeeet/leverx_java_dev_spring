package com.pg.leverx.hw3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Scope("prototype")
public class Customer implements Runnable {
    public final int customerId;
    private final Store store;

    private static final Random RANDOM_GEN = new Random();

    public Customer(int id, Store store) {
        this.customerId = id;
        this.store = store;
    }

    @Override
    public void run() {
        try {
            Order order = tryCreateOrder();
            if (order != null) {
                this.store.queuedOrders.put(order);
                System.out.println(
                        "Customer " + this.customerId + " placed an order for product "
                                + order.product.productId + " x" + order.quantity
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Order tryCreateOrder() throws InterruptedException {
        List<Map.Entry<Product, Integer>> productsInStock = this.store.getProductCatalog();
        if (productsInStock.isEmpty()) {
            return null;
        }

        // simulating product selection: randomly choosing a product from the catalog,
        // then randomly choosing the amount in range [1, quantity_in_warehouse]
        Map.Entry<Product, Integer> selectedProductEntry = productsInStock.get(RANDOM_GEN.nextInt(productsInStock.size()));
        Product product = selectedProductEntry.getKey();
        int quantity = RANDOM_GEN.nextInt(1, selectedProductEntry.getValue() + 1);
        return new Order(product, quantity, this.customerId);
    }
}
