package com.pg.leverx.hw3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
@Scope("prototype")
public class Customer implements Runnable {
    public final int customerId;
    private final Store store;
    private final ReservationService reservationService;

    private static final Random RANDOM_GEN = new Random();

    public Customer(int id, Store store, ReservationService reservationService) {
        this.customerId = id;
        this.store = store;
        this.reservationService = reservationService;
    }

    @Override
    public void run() {
        try {
            Action action = selectAction();
            switch (action) {
                case ORDER -> {
                    Order order = tryCreateOrder();
                    if (order != null) {
                        this.store.queuedOrders.put(order);
                        System.out.println(
                                "Customer " + this.customerId + " placed an order for product "
                                        + order.product.productId + " x" + order.quantity
                        );
                    }
                }
                case RESERVE -> {
                    Reservation reservation = tryCreateReservation();
                    if (reservation == null) {
                        return;
                    }

                    System.out.println(
                            "Customer " + this.customerId + " placed a reservation for product "
                                    + reservation.product.productId + " x" + reservation.quantity
                                    + " (total stock: " + reservation.getTotalStockAtReserveTime() + ")"
                    );
                    if (RANDOM_GEN.nextDouble() < 0.5) {
                        SECONDS.sleep(RANDOM_GEN.nextInt(2));
                        this.cancelReservation(reservation);
                        System.out.println(
                                "Customer " + this.customerId + " cancelled a reservation for product "
                                        + reservation.product.productId + " x" + reservation.quantity
                        );
                    }
                }
                case null, default -> throw new IllegalStateException("Unsupported action: " + action);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Order tryCreateOrder() throws InterruptedException {
        Map.Entry<Product, Integer> selectedProductEntry = trySelectRandomProductEntry();
        if (selectedProductEntry == null) {
            return null;
        }

        Product product = selectedProductEntry.getKey();
        int quantity = RANDOM_GEN.nextInt(1, selectedProductEntry.getValue() + 1);
        return new Order(product, quantity, this.customerId);
    }

    public Reservation tryCreateReservation() throws InterruptedException {
        Map.Entry<Product, Integer> selectedProductEntry = trySelectRandomProductEntry();
        if (selectedProductEntry == null) {
            return null;
        }

        Product selected = selectedProductEntry.getKey();
        Integer qtyInStock = selectedProductEntry.getValue();
        int quantity = RANDOM_GEN.nextInt(1, qtyInStock + 1);
        Reservation reservation = new Reservation(selected, this.customerId, quantity);
        boolean placed = this.reservationService.tryPlaceReservation(reservation);
        return placed ? reservation : null;
    }

    private void cancelReservation(Reservation reservation) {
        this.store.cancelReservation(reservation);
    }

    private Map.Entry<Product, Integer> trySelectRandomProductEntry() {
        List<Map.Entry<Product, Integer>> productsInStock = this.store.getProductCatalog();
        if (productsInStock.isEmpty()) {
            return null;
        }

        return productsInStock.get(RANDOM_GEN.nextInt(productsInStock.size()));
    }

    private Action selectAction() {
        if (RANDOM_GEN.nextDouble() < 0.5) {
            return Action.ORDER;
        }
        return Action.RESERVE;
    }

    enum Action {
        ORDER,
        RESERVE,
    }
}
