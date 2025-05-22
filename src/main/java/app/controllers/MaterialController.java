package app.controllers;

import app.entities.Order;
import app.entities.Orderline;
import app.services.Calculator;
import app.persistence.ConnectionPool;
import app.exceptions.DatabaseException;
import io.javalin.http.Context;
import io.javalin.Javalin;

import java.util.Map;

public class MaterialController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("/api/calculate-price", ctx -> calculatePrice(ctx, connectionPool));
    }

    private static void calculatePrice(Context ctx, ConnectionPool connectionPool) {
        try {
            int width = Integer.parseInt(ctx.formParam("width"));
            int length = Integer.parseInt(ctx.formParam("length"));

            Calculator calculator = new Calculator(width, length, connectionPool);
            Order dummyOrder = new Order(); // temporary empty order object
            calculator.calcCarport(dummyOrder);

            double costPrice = calculator.getOrderlines().stream()
                    .mapToDouble(Orderline::getOrderlinePrice)
                    .sum();

            double salesPrice = costPrice * 5; // suggested price = 5x cost

            ctx.json(Map.of(
                    "price", Math.round(salesPrice * 100.0) / 100.0,
                    "costPrice", Math.round(costPrice * 100.0) / 100.0
            ));

        } catch (NumberFormatException | DatabaseException e) {
            ctx.status(400).json(Map.of("error", "Ugyldige input eller databasefejl"));
        }
    }
}
