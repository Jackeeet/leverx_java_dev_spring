# LeverX Java Development Course Homework

## Homework 3 - Adding new functionality with Spring and AOP

### Program Description

The program simulates a store by creating several customers who place orders or reservations on products from the
store's catalog.

When a customer places a reservation, the requested quantity becomes unavailable to other customers, but it is kept in
the warehouse. A customer may cancel a reservation, returning the reserved quantity to the available stock. If a
customer tries to reserve more items than are available in stock, the reservation is not placed.

When an order is placed and taken up for processing, if there's enough of the ordered product available (in stock and
not reserved), the order will be marked as `PROCESSED_FULFILLED`, and the required quantity of the product will be removed
from the warehouse. If an order can't be fulfilled because another customer's order/reservation for the same product has
already gone through, and the warehouse no longer has the required amount of items in stock, the order will be marked as
`PROCESSED_NOT_FULFILLED`, and no extra items will be removed from the warehouse..

Store analytics are created with the custom `Analytics` aspect class. Most calculations happen with the help
of `Advice`s that are applied with the same `Pointcut` after an order is processed successfully. Another `Pointcut` +
`Advice` combo is used for analyzing the reservations after they're placed. After the `StoreThreads`'s `run` method
is executed successfully, a final `Advice` is applied, printing the calculated results.

### Program Behaviour

The program will immediately fill the warehouse with a random number of items in the range
`[1, max number of products (default 10)]`, and then output their IDs, price, and random quantity
in the range `[1, max quantity (default 20)]`.

For example, if the program is run with the parameters `10` (customers), `3` (workers), `7` (max products), `100`
(max quantity), the output may be as following:

```
There are 5 products in the warehouse:
[Product] ID: 1, price = 100.94 (x90)
[Product] ID: 2, price = 617.11 (x50)
[Product] ID: 3, price = 423.16 (x70)
[Product] ID: 4, price = 223.64 (x76)
[Product] ID: 5, price = 985.95 (x9)
```

Then, consumers will automatically start placing either reservations or orders. If a customer places a reservation,
after a while it may cancel it, returning the reserved stock to the warehouse. An order can't be cancelled.
At the same time as the customers are acting, order workers will process placed orders.
The output will be similar to the following:

``` 
Customer 2 placed an order for product 5 x1
Customer 4 placed an order for product 4 x57
Customer 5 placed an order for product 5 x6
Customer 9 placed an order for product 2 x5
Customer 10 placed an order for product 2 x29
Customer 7 placed a reservation for product 3 x2 (total stock: 70)
Customer 6 placed a reservation for product 2 x19 (total stock: 50)
Customer 6 cancelled a reservation for product 2 x19
Customer 1 placed a reservation for product 3 x65 (total stock: 70)
[Worker 1] Order 4048792427 from customer 2 fulfilled
[Worker 1] Order 8544056330 from customer 5 fulfilled
[Worker 1] Order 2753055403 from customer 10 fulfilled
[Worker 1] Order 2846195296 from customer 9 fulfilled
[Worker 2] Order 0101526378 from customer 4 fulfilled
Customer 8 placed an order for product 3 x1
[Worker 1] Order 9387759182 from customer 8 fulfilled
```

After all the orders and reservations are processed, if the `debug` option is true, the program will
output the state of the warehouse and information about the sold products, similarly to this:

``` 
----
Current warehouse status:
[Product] ID: 1, price = 100.94 (total: 90, available: 90, reserved: 0)
[Product] ID: 2, price = 617.11 (total: 16, available: 16, reserved: 0)
[Product] ID: 3, price = 423.16 (total: 69, available: 2, reserved: 67)
[Product] ID: 4, price = 223.64 (total: 19, available: 19, reserved: 0)
[Product] ID: 5, price = 985.95 (total: 2, available: 2, reserved: 0)
----
Sold products info:
[Product] ID: 2, price = 617.11 (34, 20981.74)
[Product] ID: 3, price = 423.16 (1, 423.16)
[Product] ID: 4, price = 223.64 (57, 12747.48)
[Product] ID: 5, price = 985.95 (7, 6901.65)
----
```

Regardless of the `debug` option's value, at the end the program will output the store analytics,
listing the amount of processed and fulfilled orders, the total profits, a list of top 3 bestsellers,
and information about the highest reservation percent for each product that was reserved at least once:

``` 
Store analytics:
- orders processed: 6
- orders fulfilled successfully: 6
- total profits: 41054.03
- top 3 bestsellers: 
  1) Product 4, 57 items sold
  2) Product 2, 34 items sold
  3) Product 5, 7 items sold
- highest reservation percentage per product:
  - Product 2: 38.0%
  - Product 3: 92.86%
```
