package app;

import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.controllers.MaterialController;
import app.controllers.OrderController;
import app.controllers.UserController;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main {

    private static final String USER = "postgres";
    private static final String PASSWORD = System.getenv("password"); // We have used environment variables for security. Click main -> Edit Configurations -> Environment Variables -> Add password and ip
    private static final String URL = "jdbc:postgresql://" + System.getenv("ip") + ":5432/%s?currentSchema=public";
    private static final String DB = "fog";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args) {

        // Initializing Javalin and Jetty webserver
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);

        // Add routes
        OrderController orderController = new OrderController(connectionPool);
        orderController.addRoutes(app);
        UserController userController = new UserController(connectionPool);
        userController.addRoutes(app);
        MaterialController.addRoutes(app, connectionPool);
    }
}
