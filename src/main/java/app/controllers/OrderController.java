package app.controllers;

import app.entities.*;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import app.persistence.CarportMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.List;

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

        app.get("/", ctx -> {
            try {
                List<String> roofMaterialOptions = carportMapper.getRoofMaterials();
                List<String> roofTypeOptions = List.of("Flat roof");  // Hardcoded for now

                ctx.attribute("roofMaterialOptions", roofMaterialOptions);
                ctx.attribute("roofTypeOptions", roofTypeOptions);
                ctx.render("index.html");
            } catch (SQLException e) {
                e.printStackTrace();
                ctx.status(500).result("Error retrieving roof materials.");
            }
        });
    }

    private void handleOrder(Context ctx) {
        try {
            // Receive customer data from the form
            String name = ctx.formParam("name");
            String address = ctx.formParam("address");
            int zip = Integer.parseInt(ctx.formParam("zip"));
            String cityName = ctx.formParam("city");
            int phone = Integer.parseInt(ctx.formParam("phoneNumber"));
            String email = ctx.formParam("email");

            // Create customer and save to database
            Customer customer = new Customer(name, address, zip, cityName, phone, email);
            userMapper.saveUser(customer);

            // Get available salesperson for this order
            Salesperson salesperson = userMapper.assignSalesperson();

            // Receive carport data from the form
            int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
            int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
            int carportHeight = Integer.parseInt(ctx.formParam("carportHeight"));

            Carport carport = new Carport(carportHeight, carportLength, carportWidth);

            // Check for optional shed
            String shedToggle = ctx.formParam("shedToggle");
            Integer shedWidth = null;
            Integer shedLength = null;

            if ("with".equals(shedToggle)) {
                String shedWidthParam = ctx.formParam("shedWidth");
                String shedLengthParam = ctx.formParam("shedLength");

                // Only add shed if both fields are filled
                if (shedWidthParam != null && !shedWidthParam.isBlank() &&
                        shedLengthParam != null && !shedLengthParam.isBlank()) {
                    shedWidth = Integer.parseInt(shedWidthParam);
                    shedLength = Integer.parseInt(shedLengthParam);
                    carport.setShedWidth(shedWidth);
                    carport.setShedLength(shedLength);
                    System.out.println("Shed Width: " + shedWidth);
                    System.out.println("Shed Length: " + shedLength);
                }
            }

            // Save carport to database
            carportMapper.saveCarport(carport);

            // Create and save order
            Order order = new Order();
            order.setCustomer(customer);
            order.setCarport(carport);
            order.setPrice(25000);  // Hardcoded price. TODO: add dynamic price calculation
            order.setStatus("Ny ordre");
            order.setDeliveryDate(null);
            order.setSalesperson(salesperson);
            order.setCarportHeight(carportHeight);
            order.setCarportWidth(carportWidth);
            order.setCarportLength(carportLength);

            // Set shed dimensions if available
            order.setShedWidth(shedWidth != null ? shedWidth : 0);
            order.setShedLength(shedLength != null ? shedLength : 0);

            orderMapper.saveOrder(order);

            // Save order in session
            ctx.sessionAttribute("order", order);

            // Redirect to thank you page
            ctx.redirect("/tak-for-din-ordre");

        } catch (NumberFormatException e) {
            e.printStackTrace();
            ctx.status(400).result("Error: Please fill out all fields correctly.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Something went wrong. Please try again.");
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
