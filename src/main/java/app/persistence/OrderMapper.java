package app.persistence;

import app.entities.Customer;
import app.entities.Order;
import app.entities.Salesperson;
import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class OrderMapper {

    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void saveOrder(Order order) throws SQLException {
        String sql = """
                INSERT INTO orders (customer_number, carport_height, carport_width, carport_length, shed_width, shed_length, status, order_price, cost_price, salesperson_id, svg, order_date) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set a customer number (phone number)
            stmt.setInt(1, order.getCustomer().getPhoneNumber());
            stmt.setInt(2, order.getCarportHeight());
            stmt.setInt(3, order.getCarportWidth());
            stmt.setInt(4, order.getCarportLength());
            stmt.setObject(5, order.getShedWidth() != 0 ? order.getShedWidth() : null, java.sql.Types.INTEGER);
            stmt.setObject(6, order.getShedLength() != 0 ? order.getShedLength() : null, java.sql.Types.INTEGER);
            stmt.setString(7, order.getStatus());
            stmt.setDouble(8, order.getPrice());
            stmt.setDouble(9, order.getCostPrice());
            // Set salesperson ID directly from the order
            stmt.setInt(10, order.getSalesperson().getSalespersonId());
            // Set SVG if it exists, otherwise set it to null
            stmt.setString(11, order.getSvg() != null ? order.getSvg() : null);
            stmt.setDate(12, java.sql.Date.valueOf(order.getOrderDate()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setOrderId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    public List<Order> getAllOrdersForSalesPerson() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT
                    o.order_id,
                    o.status,
                    o.order_date,
                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.zip AS customer_zip,
                    ci.city_name AS customer_city,
                    c.email AS customer_email,
                    c.phone_number AS customer_phone_number,
                    s.salesperson_id,
                    s.name AS salesperson_name
                FROM orders o
                LEFT JOIN customer c ON o.customer_number = c.phone_number
                LEFT JOIN city ci ON c.zip = ci.zip
                LEFT JOIN salesperson s ON o.salesperson_id = s.salesperson_id;
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(rs.getString("status") != null ? rs.getString("status") : "Ingen status");
                Date orderDate = rs.getDate("order_date");
                order.setOrderDate(orderDate != null ? orderDate.toLocalDate() : null);

                // Create Customer
                Customer customer = new Customer();
                customer.setName(rs.getString("customer_name"));
                customer.setAddress(rs.getString("customer_address"));
                customer.setZip(rs.getInt("customer_zip"));
                customer.setCity(rs.getString("customer_city"));
                customer.setEmail(rs.getString("customer_email"));
                customer.setPhoneNumber(rs.getInt("customer_phone_number"));
                order.setCustomer(customer);

                // Create Salesperson
                int salespersonId = rs.getInt("salesperson_id");
                if (!rs.wasNull()) {
                    Salesperson salesperson = new Salesperson();
                    salesperson.setSalespersonId(salespersonId);
                    salesperson.setName(rs.getString("salesperson_name"));
                    order.setSalesperson(salesperson);
                }

                orders.add(order);
            }
        }
        return orders;
    }

    public Order getOrderById(int orderId) throws SQLException {
        String sql = """
                SELECT
                           o.order_id,
                           o.status,
                           o.order_date,
                           o.carport_height,
                           o.carport_width,
                           o.carport_length,
                           o.shed_width,
                           o.shed_length,
                           o.svg,
                           o.order_price,
                           o.cost_price,
                           c.name AS customer_name,
                           c.address AS customer_address,
                           c.zip AS customer_zip,
                           c.email AS customer_email,
                           c.phone_number AS customer_phone,
                           ci.city_name AS customer_city,
                           s.salesperson_id,
                           s.name AS salesperson_name,
                           s.email AS salesperson_email,
                           s.phone_number AS salesperson_phone
                       FROM orders o
                       LEFT JOIN customer c ON o.customer_number = c.phone_number
                       LEFT JOIN city ci ON c.zip = ci.zip
                       LEFT JOIN salesperson s ON o.salesperson_id = s.salesperson_id
                       WHERE o.order_id = ?;
                
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setStatus(rs.getString("status"));
                    order.setOrderDate(rs.getDate("order_date").toLocalDate());
                    order.setCarportHeight(rs.getInt("carport_height"));
                    order.setCarportWidth(rs.getInt("carport_width"));
                    order.setCarportLength(rs.getInt("carport_length"));
                    order.setSvg(rs.getString("svg"));
                    order.setPrice(rs.getInt("order_price"));
                    order.setCostPrice(rs.getInt("cost_price"));

                    // Shed - only if it is not null
                    int shedWidth = rs.getInt("shed_width");
                    if (!rs.wasNull()) {
                        order.setShedWidth(shedWidth);
                    }
                    int shedLength = rs.getInt("shed_length");
                    if (!rs.wasNull()) {
                        order.setShedLength(shedLength);
                    }

                    // Set customer
                    Customer customer = new Customer();
                    customer.setName(rs.getString("customer_name"));
                    customer.setAddress(rs.getString("customer_address"));
                    customer.setZip(rs.getInt("customer_zip"));
                    customer.setEmail(rs.getString("customer_email"));
                    customer.setPhoneNumber(rs.getInt("customer_phone"));
                    customer.setCity(rs.getString("customer_city"));
                    order.setCustomer(customer);

                    // Set salesperson
                    int salespersonId = rs.getInt("salesperson_id");
                    if (!rs.wasNull()) {
                        Salesperson salesperson = new Salesperson();
                        salesperson.setSalespersonId(salespersonId);
                        salesperson.setName(rs.getString("salesperson_name"));
                        salesperson.setEmail(rs.getString("salesperson_email"));
                        salesperson.setPhoneNumber(rs.getInt("salesperson_phone"));
                        order.setSalesperson(salesperson);
                    }
                    return order;
                }
            }
        }
        return null;
    }

    public void updateFullOrder(Order order) throws SQLException {
        String sql = """
                UPDATE orders SET
                    carport_height = ?,
                    carport_width = ?,
                    carport_length = ?,
                    shed_width = ?,
                    shed_length = ?,
                    order_price = ?,
                    svg = ?
                WHERE order_id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getCarportHeight());
            stmt.setInt(2, order.getCarportWidth());
            stmt.setInt(3, order.getCarportLength());
            stmt.setObject(4, order.getShedWidth() != 0 ? order.getShedWidth() : null, Types.INTEGER);
            stmt.setObject(5, order.getShedLength() != 0 ? order.getShedLength() : null, Types.INTEGER);
            stmt.setDouble(6, order.getPrice());
            stmt.setString(7, order.getSvg());
            stmt.setInt(8, order.getOrderId());

            stmt.executeUpdate();
        }
    }

    public void updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }


    public List<Orderline> getOrderlinesForOrder(int orderId) throws SQLException {
        List<Orderline> orderlines = new ArrayList<>();
        String sql = """
                SELECT o.orderline_id, o.quantity, o.build_description, o.orderline_price,
                       v.variant_id, v.length,
                       m.material_id, m.name, m.price, m.unit, m.width, m.height
                FROM orderline o
                JOIN variant v ON o.variant_id = v.variant_id
                JOIN material m ON v.material_id = m.material_id
                WHERE o.order_id = ?
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Material material = new Material(
                        rs.getInt("material_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("unit"),
                        rs.getInt("quantity"),
                        rs.getInt("width"),
                        rs.getInt("height")
                );

                Variant variant = new Variant(
                        rs.getInt("variant_id"),
                        rs.getInt("length"),
                        material
                );

                Orderline orderline = new Orderline(
                        rs.getInt("orderline_id"),
                        null, // Order will be set later
                        variant,
                        rs.getInt("quantity"),
                        rs.getString("build_description"),
                        rs.getDouble("orderline_price")
                );
                orderlines.add(orderline);
            }
        }

        return orderlines;
    }

    public void saveOrderlines(List<Orderline> orderlines, int orderId) throws SQLException {
        String sql = """
                    INSERT INTO orderline (order_id, variant_id, quantity, build_description, orderline_price)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Orderline orderline : orderlines) {
                ps.setInt(1, orderId);
                ps.setInt(2, orderline.getVariant().getVariantId());
                ps.setInt(3, orderline.getQuantity());
                ps.setString(4, orderline.getBuildDescription());
                ps.setDouble(5, orderline.getOrderlinePrice());
                ps.addBatch();  // Optimized batch insert
            }
            ps.executeBatch(); // Execute all at once
        }
    }
}
