package com.pg.leverx.hw3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class OrderWorker implements Runnable {
    private final int workerId;
    private final Store store;
    private final OrderFulfillmentService orderFulfillmentService;

    public OrderWorker(int id, Store store, OrderFulfillmentService orderFulfillmentService) {
        this.workerId = id;
        this.store = store;
        this.orderFulfillmentService = orderFulfillmentService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = this.store.queuedOrders.poll(5, TimeUnit.SECONDS);
                if (order == null) {
                    return;
                }

                boolean fulfilled = orderFulfillmentService.tryFulfillOrder(order);
                String resultMessage = "[Worker " + this.workerId + "] Order " + order.orderId
                        + " from customer " + order.customerId;
                System.out.println(resultMessage + (fulfilled ? " fulfilled" : " not fulfilled"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
