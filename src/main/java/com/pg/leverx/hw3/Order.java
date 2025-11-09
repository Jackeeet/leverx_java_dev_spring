package com.pg.leverx.hw3;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Objects;

public class Order {
    private static final RandomStringUtils ID_GENERATOR = RandomStringUtils.insecure();

    public final String orderId;
    public final Product product;
    public final int customerId;
    public final Integer quantity;

    public enum OrderStatus {
        NOT_PROCESSED,
        PROCESSED_FULFILLED,
        PROCESSED_NOT_FULFILLED
    }

    private OrderStatus status;

    public Order(Product product, int quantity, int customerId) {
        this.orderId = ID_GENERATOR.nextNumeric(10);
        this.product = product;
        this.quantity = quantity;
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public void setStatus(OrderStatus status) {
        if (this.status == OrderStatus.PROCESSED_FULFILLED) {
            throw new IllegalStateException("Order is already fulfilled");
        }
        if (this.status == OrderStatus.PROCESSED_NOT_FULFILLED) {
            throw new IllegalStateException("Order is already processed");
        }

        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return customerId == order.customerId
                && Objects.equals(orderId, order.orderId)
                && Objects.equals(product, order.product)
                && Objects.equals(quantity, order.quantity)
                && status == order.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, product, customerId, quantity, status);
    }
}
