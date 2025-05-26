package app.controllers;

import app.entities.*;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import app.persistence.CarportMapper;
import app.services.CarportSvg;
import app.services.Calculator;
import app.services.EmailSender;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final CarportMapper carportMapper;
    private final ConnectionPool connectionPool;

    public OrderController(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.orderMapper = new OrderMapper(connectionPool);
        this.userMapper = new UserMapper(connectionPool);
        this.carportMapper = new CarportMapper(connectionPool);
    }

    public void addRoutes(Javalin app) {
        app.post("/order", this::handleOrder);
        app.get("/summary", this::showSummary);
        app.get("/", this::showOrder);
        app.post("/update-sketch", this::updateSketch);
        app.get("/city/{zip}", this::getCityByZip);

        app.get("/tak-for-din-ordre", this::showThankYouPage);

        // Admin routes
        app.get("/admin/alle-ordrer", this::showOrdersPage);
        app.get("/admin/ordre/{id}", this::showOrderDetails);
        app.get("/admin/ordre/preview/{id}", this::previewOrder);
        app.post("/admin/ordre/send", this::sendOrderToCustomer);
        app.post("/admin/update-order", this::updateFullOrder);
    }

    private void showThankYouPage(Context ctx) {
        Order order = ctx.sessionAttribute("order");
        if (order != null) {
            ctx.render("tak-for-din-ordre.html");
        } else {
            ctx.redirect("/");
        }
    }

    private void previewOrder(Context ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        try {
            Order order = orderMapper.getOrderById(orderId);
            ctx.attribute("order", order);
            ctx.render("ordre-oversigt.html");
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved hentning af ordre.");
        }
    }

    private void sendOrderToCustomer(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            orderMapper.updateOrderStatus(orderId, "Sendt til kunde");

            Order order = orderMapper.getOrderById(orderId);
            EmailSender emailSender = new EmailSender();
            emailSender.sendFinalOfferEmail(order);

            ctx.sessionAttribute("message", "Tilbud sendt til kunden.");
            ctx.redirect("/admin/alle-ordrer");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved afsendelse af e-mail.");
        }
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

            // Generate SVG based on carport dimensions
            CarportSvg carportSvg = new CarportSvg(carportWidth, carportLength);
            String svgString = carportSvg.toString();

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
                }
            }
            // Save carport to database
            carportMapper.saveCarport(carport);

            // Create and save order
            Order order = new Order();
            order.setCustomer(customer);
            order.setCarport(carport);
            Calculator calculator = new Calculator(carportWidth, carportLength, connectionPool);
            calculator.calculateAndSetPrices(order);
            order.setStatus("Ny ordre");
            order.setDeliveryDate(null);
            order.setSalesperson(salesperson);
            order.setCarportHeight(carportHeight);
            order.setCarportWidth(carportWidth);
            order.setCarportLength(carportLength);
            // Set shed dimensions if available
            order.setShedWidth(shedWidth != null ? shedWidth : 0);
            order.setShedLength(shedLength != null ? shedLength : 0);
            // Add the generated SVG to the order
            order.setSvg(svgString);
            // Set order date automatically to today's date
            order.setOrderDate(LocalDate.now());

            orderMapper.saveOrder(order);

            // Send email to customer
            EmailSender emailSender = new EmailSender();
            emailSender.sendOrderConfirmationEmail(order);

            // Save orderlines to database
            List<Orderline> orderlines = calculator.getOrderlines();
            orderMapper.saveOrderlines(orderlines, order.getOrderId());

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

    private void showOrder(Context ctx) {
        try {
            // SVG for carport
            CarportSvg svg = new CarportSvg(500, 500);
            ctx.attribute("svg", svg.toString());

            // Get roof-types
            List<String> roofMaterialOptions = carportMapper.getRoofMaterials();
            List<String> roofTypeOptions = List.of("Flat roof");  // Hardcoded for now

            ctx.attribute("roofMaterialOptions", roofMaterialOptions);
            ctx.attribute("roofTypeOptions", roofTypeOptions);

            // Render index.html
            ctx.render("index.html");
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Error retrieving roof materials.");
        }
    }

    private void updateSketch(Context ctx) {
        try {
            // Get user input
            int width = Integer.parseInt(ctx.formParam("carportWidth"));
            int length = Integer.parseInt(ctx.formParam("carportLength"));

            // Generate SVG based on user input
            CarportSvg carportSvg = new CarportSvg(width, length);

            // Send SVG to frontend
            ctx.result(carportSvg.toString());

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).result("Kunne ikke opdatere skitse.");
        }
    }

    public void showOrdersPage(Context ctx) {
        if (!UserController.isAdmin(ctx)) {
            ctx.redirect("/login");
            return;
        }
        try {
            List<Order> orders = orderMapper.getAllOrdersForSalesPerson();
            if (orders == null) {
                orders = new ArrayList<>();
            }
            // Get message from session and send to Thymeleaf
            String message = ctx.sessionAttribute("message");
            if (message != null) {
                ctx.attribute("message", message);
                ctx.sessionAttribute("message", null); // remove after viewing
            }
            ctx.attribute("orders", orders);
            UserController.addAdminNameAttribute(ctx); // Admin name
            ctx.render("alle-ordrer.html");

        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved hentning af ordrer.");
        }
    }

    private void showOrderDetails(Context ctx) {
        if (!UserController.isAdmin(ctx)) {
            ctx.redirect("/login");
            return;
        }
        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Order order = orderMapper.getOrderById(orderId);
            if (order != null) {
                List<Orderline> orderlines = orderMapper.getOrderlinesForOrder(orderId);
                ctx.attribute("order", order);
                ctx.attribute("orderlines", orderlines);
                ctx.attribute("svg", order.getSvg());

                ctx.render("ordre-detaljer.html");
            } else {
                ctx.status(404).result("Ordre ikke fundet");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved hentning af ordre.");
        }
    }

    private void updateFullOrder(Context ctx) {
        if (!UserController.isAdmin(ctx)) {
            ctx.redirect("/login");
            return;
        }
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
            int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
            int carportHeight = Integer.parseInt(ctx.formParam("carportHeight"));
            double newPrice = Double.parseDouble(ctx.formParam("orderPrice"));
            double costPrice = Double.parseDouble(ctx.formParam("costPrice"));

            // Shed
            int shedWidth = 0;
            int shedLength = 0;
            String shedToggle = ctx.formParam("shedToggle");
            if ("with".equals(shedToggle)) {
                shedWidth = Integer.parseInt(ctx.formParam("shedWidth"));
                shedLength = Integer.parseInt(ctx.formParam("shedLength"));
            }

            // Generate new SVG based on updated dimensions
            CarportSvg carportSvg = new CarportSvg(carportWidth, carportLength);
            String newSvg = carportSvg.toString();

            // Get existing order and overwrite fields
            Order order = orderMapper.getOrderById(orderId);
            order.setCarportWidth(carportWidth);
            order.setCarportLength(carportLength);
            order.setCarportHeight(carportHeight);
            order.setShedWidth(shedWidth);
            order.setShedLength(shedLength);
            order.setPrice(newPrice);
            order.setCostPrice(costPrice);
            order.setSvg(newSvg);

            orderMapper.updateFullOrder(order);

            ctx.redirect("/admin/ordre/" + orderId);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).result("Kunne ikke opdatere ordren.");
        }
    }
}
