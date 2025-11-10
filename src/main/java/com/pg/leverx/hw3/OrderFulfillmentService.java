package com.pg.leverx.hw3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

// moved the order fulfillment logic to a separate bean to be able to do AOP-based analytics
@Service
@Scope("prototype")
public class OrderFulfillmentService {
    private final Store store;

    public OrderFulfillmentService(Store store) {
        this.store = store;
    }

    public boolean tryFulfillOrder(Order order) throws InterruptedException {
        AtomicBoolean success = new AtomicBoolean(false);
        this.store.warehouse.computeIfPresent(order.product, (_, qtyInStock) -> {
            if (qtyInStock.available < order.quantity) {
                // if a customer ordered more than currently available (because another order
                // for the same product already went through and some items were sold),
                // the order can't be fulfilled and will be marked as PROCESSED_NOT_FULFILLED
                return qtyInStock;
            }

            success.set(true);
            return new StockQuantity(qtyInStock.available - order.quantity, qtyInStock.reserved);
        });

        order.setStatus(success.get() ?
                Order.OrderStatus.PROCESSED_FULFILLED :
                Order.OrderStatus.PROCESSED_NOT_FULFILLED
        );
        this.store.processedOrders.add(order);

        return success.get();
    }
}
