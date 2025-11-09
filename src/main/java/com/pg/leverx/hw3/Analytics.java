package com.pg.leverx.hw3;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class Analytics {
    private final AtomicInteger processedOrderCount = new AtomicInteger(0);
    private final AtomicInteger fulfilledOrderCount = new AtomicInteger(0);
    private BigDecimal totalProfit = BigDecimal.ZERO;
    private final ConcurrentHashMap<Product, Integer> soldProductsQuantity = new ConcurrentHashMap<>();

    @Pointcut("execution(* com.pg.leverx.hw3.OrderFulfillmentService.*(..))")
    public void orderProcessed() {
    }

    @AfterReturning("orderProcessed()")
    public void incrementProcessedCount() {
        int _ = this.processedOrderCount.incrementAndGet();
    }

    @AfterReturning(value = "orderProcessed()", returning = "success")
    public void incrementFulfilledCount(boolean success) {
        if (success) {
            int _ = this.fulfilledOrderCount.incrementAndGet();
        }
    }

    @AfterReturning(value = "orderProcessed() && args(order)", returning = "success")
    public void updateTotalProfit(boolean success, Order order) {
        if (success) {
            this.addProfit(order.product.price.multiply(BigDecimal.valueOf(order.quantity)));
        }
    }

    @AfterReturning(value = "orderProcessed() && args(order)", returning = "success")
    public void updateProductsSoldQuantity(boolean success, Order order) {
        if (success) {
            this.soldProductsQuantity.merge(order.product, order.quantity, Integer::sum);
        }
    }

    private synchronized void addProfit(BigDecimal sum) {
        this.totalProfit = this.totalProfit.add(sum);
    }

    @AfterReturning("execution(* com.pg.leverx.hw3.StoreThreads.run(..))")
    public void reportAnalytics() {
        List<Map.Entry<Product, Integer>> bestsellers = this.soldProductsQuantity.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .toList();

        StringBuilder message = new StringBuilder("Store analytics:")
                .append("\n- orders processed: ").append(this.processedOrderCount.get())
                .append("\n- orders fulfilled successfully: ").append(this.fulfilledOrderCount.get())
                .append("\n- total profits: ").append(this.totalProfit)
                .append("\n- top ").append(bestsellers.size()).append(" bestsellers: ");
        for (int i = 0; i < bestsellers.size(); i++) {
            Map.Entry<Product, Integer> bestseller = bestsellers.get(i);
            message.append("\n  ").append(i + 1)
                    .append(") Product ").append(bestseller.getKey().productId)
                    .append(", ").append(bestseller.getValue()).append(" items sold");
        }

        System.out.println(message);
    }
}
