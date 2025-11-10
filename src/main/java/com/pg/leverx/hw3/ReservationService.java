package com.pg.leverx.hw3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

// a separate bean to be able to do AOP-based analytics, like orderFulfillmentService
@Service
@Scope("prototype")
public class ReservationService {
    private final Store store;

    public ReservationService(Store store) {
        this.store = store;
    }

    public boolean tryPlaceReservation(Reservation reservation) {
        return store.placeReservation(reservation);
    }
}
