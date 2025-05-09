package app.controllers;

import app.entities.Carport;
import app.entities.Customer;
import app.entities.Order;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import app.persistence.CarportMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class OrderController {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final CarportMapper carportMapper;

    public OrderController(ConnectionPool connectionPool) {
        this.orderMapper = new OrderMapper(connectionPool);
        this.userMapper = new UserMapper(connectionPool);
        this.carportMapper = new CarportMapper(connectionPool);
    }

    public void addRoutes(Javalin app) {

        app.post("/order", this::handleOrder);
        app.get("/summary", this::showSummary);
    }

    private void handleOrder(Context ctx) {
        // Receive data from the form
        String name = ctx.formParam("name");
        String address = ctx.formParam("address");
        int zip = Integer.parseInt(ctx.formParam("zipcode"));
        String city = ctx.formParam("city");
        int phone = Integer.parseInt(ctx.formParam("phone"));
        String email = ctx.formParam("email");

        int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
        int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
        int carportHeight = Integer.parseInt(ctx.formParam("carportHeight"));

        // Create customer
        Customer customer = new Customer(name, address, zip, city, phone, email);
        userMapper.saveUser(customer);

        // Create carport
        Carport carport = new Carport(carportHeight, carportLength, carportWidth);
        carportMapper.saveCarport(carport);

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setCarport(carport);
        order.setPrice(25000);  // Hardcoded pris. TODO: code price calc. method
        order.setStatus("Ny ordre");
        order.setDeliveryDate(null);

        orderMapper.saveOrder(order);

        // Gem ordre i sessionen
        ctx.sessionAttribute("order", order);

        // Redirect til kvitteringsside
        ctx.redirect("/summary");
    }

    private void showSummary(Context ctx) {
        Order order = ctx.sessionAttribute("order");
        ctx.json(order);
    }
}
