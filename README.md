# LeverX Java Development Course Homework

## Homework 3 - Adding new functionality with Spring and AOP

### Program Description

The program simulates a store by creating several customers who place orders on products from the store's catalog.

The entry point for the app is the `StoreThreads` class. It implements the `CommandLineRunner` interface 
to keep the previous project's behaviour.

The catalog is created on-demand based on the contents of the warehouse, which is represented
by a `ConcurrentHashMap<Product, Integer>`.

The program is based on the producer-consumer pattern, where producers (`Customer`s) put their orders into
a `BlockingQueue<Order>`, and consumers (`OrderWorker`s) take them from the queue and try fulfilling them.

When processing an order, the `OrderWorker` uses the `ConcurrentHashMap.computeIfPresent()` method to safely
update the remaining product quantity in the warehouse.

If there's enough of the ordered product in stock, the order will be marked as `PROCESSED_FULFILLED`, and the required
quantity of the product will be removed from the warehouse.
If an order can't be fulfilled because another customer's order for the same product has already gone through,
and the warehouse no longer has the required amount of items in stock, the order will be marked
as `PROCESSED_NOT_FULFILLED`, and no extra items will be removed from the warehouse.

Store analytics are created with the custom `Analytics` aspect class. Most calculations happen with the help 
of `Advice`s that are applied with the same `Pointcut` after an order is processed successfully. Then, after 
the `StoreThreads`'s `run` method is executed successfully, a final `Advice` is applied, printing the calculated results.

### Program Behaviour

The program will immediately fill the warehouse with a random number of items in the range
`[1, max number of products (default 10)]`, and then output their IDs, price, and random quantity
in the range `[1, max quantity (default 20)]`.

For example, if the program is run with the parameters `5` (customers), `3` (workers), `7` (max products), `10` (max
quantity),
the output may be as following:

```
There are 6 products in the warehouse:
[Product] ID: 1, price = 54.37 (x4)
[Product] ID: 2, price = 346.26 (x8)
[Product] ID: 3, price = 421.94 (x6)
[Product] ID: 4, price = 235.81 (x3)
[Product] ID: 5, price = 310.72 (x6)
[Product] ID: 6, price = 60.03 (x1)
```

Then, consumers will automatically start placing orders, while the workers will start processing them.
The output will be similar to the following:

``` 
Customer 2 placed an order for product 5 x1
Customer 1 placed an order for product 4 x2
Customer 3 placed an order for product 6 x1
[Worker 1] Order 5772664647 from customer 2 fulfilled
[Worker 3] Order 0289178314 from customer 3 fulfilled
Customer 4 placed an order for product 4 x2
[Worker 2] Order 2418185817 from customer 1 fulfilled
Customer 5 placed an order for product 3 x3
[Worker 3] Order 8116789800 from customer 4 not fulfilled
[Worker 1] Order 2297412240 from customer 5 fulfilled
```

After all the orders are processed, the program will output the store analytics similar to the following:

``` 
Store analytics:
- orders processed: 5
- orders fulfilled successfully: 4
- total profits: 2108.19
- top 3 bestsellers: 
  1) Product 3, 3 items sold
  2) Product 4, 2 items sold
  3) Product 5, 1 items sold
```
