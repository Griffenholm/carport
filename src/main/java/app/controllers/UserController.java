package app.controllers;

import app.entities.Salesperson;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class UserController {

    private final UserMapper userMapper;

    public UserController(ConnectionPool connectionPool) {
        this.userMapper = new UserMapper(connectionPool);
    }

    public void addRoutes(Javalin app) {
        // Login-side
        app.get("/login", ctx -> ctx.render("login.html"));

        // Login
        app.post("/login", ctx -> {
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");

            try {
                Salesperson salesperson = userMapper.getSalespersonByEmailAndPassword(email, password);
                if (salesperson != null && salesperson.isAdmin()) {
                    ctx.sessionAttribute("currentUser", salesperson);
                    ctx.redirect("/admin/alle-ordrer");
                } else {
                    ctx.attribute("error", "Ugyldige loginoplysninger eller manglende adgang.");
                    ctx.render("login.html");
                }
            } catch (Exception e) {
                ctx.attribute("error", "Login mislykkedes.");
                ctx.render("login.html");
            }
        });

        // Logout
        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/login");
        });
    }

    // Used by other controllers
     public static boolean isAdmin(Context ctx) {
        Salesperson user = ctx.sessionAttribute("currentUser");
        return user != null && user.isAdmin();
    }

    public static void addAdminNameAttribute(Context ctx) {
        Salesperson user = ctx.sessionAttribute("currentUser");
        if (user != null && user.isAdmin()) {
            ctx.attribute("adminName", user.getName());
        }
    }
}
