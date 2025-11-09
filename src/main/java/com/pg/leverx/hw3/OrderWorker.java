package com.pg.leverx.hw3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Scope("prototype")
public class OrderWorker implements Runnable {
    private final int workerId;
    private final Store store;

    public OrderWorker(int id, Store store) {
        this.workerId = id;
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = this.store.queuedOrders.poll(5, TimeUnit.SECONDS);
                if (order == null) {
                    return;
                }

                boolean fulfilled = tryFulfillOrder(order);
                String resultMessage = "[Worker " + this.workerId + "] Order " + order.orderId
                        + " from customer " + order.customerId;
                System.out.println(resultMessage + (fulfilled ? " fulfilled" : " not fulfilled"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean tryFulfillOrder(Order order) throws InterruptedException {
        AtomicBoolean success = new AtomicBoolean(false);
        this.store.warehouse.computeIfPresent(order.product, (_, qtyInStock) -> {
            if (qtyInStock < order.quantity) {
                // if a customer ordered more than currently available (because another order
                // for the same product already went through and some items were sold),
                // the order can't be fulfilled and will be marked as PROCESSED_NOT_FULFILLED
                return qtyInStock;
            }

            success.set(true);
            return qtyInStock - order.quantity;
        });

        order.setStatus(success.get() ?
                Order.OrderStatus.PROCESSED_FULFILLED :
                Order.OrderStatus.PROCESSED_NOT_FULFILLED
        );
        this.store.processedOrders.add(order);

        return success.get();
    }
}
