package com.pg.leverx.hw3;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    public final int productId;
    public final BigDecimal price;

    public Product(int id, BigDecimal price) {
        this.productId = id;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId == product.productId && Objects.equals(price, product.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, price);
    }

    @Override
    public String toString() {
        return "[Product] ID: " + productId + ", price = " + price;
    }
}
