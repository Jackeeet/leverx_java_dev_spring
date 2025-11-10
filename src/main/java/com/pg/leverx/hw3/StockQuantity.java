package com.pg.leverx.hw3;

public class StockQuantity {
    public final Integer available;
    public final Integer reserved;

    StockQuantity(Integer available, Integer reserved) {
        this.available = available;
        this.reserved = reserved;
    }

    public Integer total() {
        return this.available + this.reserved;
    }

    @Override
    public String toString() {
        return "total: " + this.total() + ", available: " + this.available + ", reserved: " + this.reserved;
    }
}
