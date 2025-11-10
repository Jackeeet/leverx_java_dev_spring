package com.pg.leverx.hw3;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Objects;

public class Reservation {
    private static final RandomStringUtils ID_GENERATOR = RandomStringUtils.insecure();

    public final String reservationId;
    public final Product product;
    public final int customerId;
    public final Integer quantity;
    private Integer totalStockAtReserveTime;

    public Reservation(Product product, int customerId, Integer quantity) {
        this.reservationId = ID_GENERATOR.nextNumeric(6);
        this.product = product;
        this.customerId = customerId;
        this.quantity = quantity;
        this.totalStockAtReserveTime = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return customerId == that.customerId && Objects.equals(reservationId, that.reservationId)
                && Objects.equals(product, that.product) && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, product, customerId, quantity);
    }

    public Integer getTotalStockAtReserveTime() {
        return totalStockAtReserveTime;
    }

    public void setTotalStockAtReserveTime(Integer totalStockAtReserveTime) {
        this.totalStockAtReserveTime = totalStockAtReserveTime;
    }
}


