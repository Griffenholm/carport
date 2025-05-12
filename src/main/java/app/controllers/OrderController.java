package app.controllers;

import app.entities.Carport;
import app.entities.City;
import app.entities.Customer;
import app.entities.Order;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import app.persistence.CarportMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;

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
        app.get("/city/{zip}", this::getCityByZip);
        app.get("/tak-for-din-ordre", ctx -> {
            Order order = ctx.sessionAttribute("order");
            if (order != null) {
                ctx.render("tak-for-din-ordre.html");
            } else {
                ctx.redirect("/");
            }
        });
    }

    private void handleOrder(Context ctx) {
        try {
            // Receive customer data from the form
            String name = ctx.formParam("name");
            String address = ctx.formParam("address");
            int zip = Integer.parseInt(ctx.formParam("zipcode"));
            String cityName = ctx.formParam("city");
            int phone = Integer.parseInt(ctx.formParam("phone"));
            String email = ctx.formParam("email");

            // Validate city-zip combination
            if (cityName == null || cityName.trim().isEmpty()) {
                ctx.result("Postnummer og by passer ikke sammen.");
                return;
            }

            // Create customer
            Customer customer = new Customer(name, address, zip, cityName, phone, email);
            userMapper.saveUser(customer);

            // Receive carport data from the form
            int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
            int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
            int carportHeight = Integer.parseInt(ctx.formParam("carportHeight"));  // Default 230
            String roofType = ctx.formParam("roofType");
            String roofMaterial = ctx.formParam("roofMaterial");
            int roofSlope = Integer.parseInt(ctx.formParam("roofSlope"));

            // Create carport
            Carport carport = new Carport(carportHeight, carportLength, carportWidth);
            carport.setRoofType(roofType);
            carport.setRoofMaterial(roofMaterial);
            carport.setRoofSlope(roofSlope);
            carportMapper.saveCarport(carport);

            // Create order
            Order order = new Order();
            order.setCustomer(customer);
            order.setCarport(carport);
            order.setPrice(25000);  // Hardcoded price. TODO: code price calc. method
            order.setStatus("Ny ordre");
            order.setDeliveryDate(null);

            orderMapper.saveOrder(order);

            // Save order in session
            ctx.sessionAttribute("order", order);

            // Redirect to Thank you for your order site
            ctx.redirect("/tak-for-din-ordre");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Noget gik galt. Prøv igen.");
        }
    }

    private void showSummary(Context ctx) {
        Order order = ctx.sessionAttribute("order");
        ctx.json(order);
    }
    private void getCityByZip(Context ctx) {
        try {
            int zip = Integer.parseInt(ctx.pathParam("zip"));
            City city = userMapper.getCityByZip(zip);
            if (city != null) {
                ctx.json(city);
            } else {
                ctx.status(404).result("City not found");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid zip code format");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
