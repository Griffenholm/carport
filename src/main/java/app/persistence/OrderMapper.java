package app.persistence;

import app.entities.Customer;
import app.entities.Order;
import app.entities.Salesperson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void saveOrder(Order order) throws SQLException {
        String sql = """
                INSERT INTO orders (customer_number, carport_height, carport_width, carport_length, shed_width, shed_length, status, order_price, start_price, salesperson_id, svg, order_date) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getCustomer().getPhoneNumber());
            stmt.setInt(2, order.getCarportHeight());
            stmt.setInt(3, order.getCarportWidth());
            stmt.setInt(4, order.getCarportLength());
            stmt.setObject(5, order.getShedWidth() != 0 ? order.getShedWidth() : null, java.sql.Types.INTEGER);
            stmt.setObject(6, order.getShedLength() != 0 ? order.getShedLength() : null, java.sql.Types.INTEGER);
            stmt.setString(7, order.getStatus());
            stmt.setInt(8, order.getPrice());
            stmt.setInt(9, order.getPrice()); // Assuming start price is the same as order price for now
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
            s.salesperson_id, 
            s.name AS salesperson_name 
        FROM orders o 
        LEFT JOIN customer c ON o.customer_number = c.phone_number 
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
                String customerName = rs.getString("customer_name");
                customer.setName(customerName != null ? customerName : "Ukendt kunde");
                order.setCustomer(customer);

                // Create Salesperson
                int salespersonId = rs.getInt("salesperson_id");
                if (!rs.wasNull()) {
                    Salesperson salesperson = new Salesperson();
                    salesperson.setSalespersonId(salespersonId);
                    String salespersonName = rs.getString("salesperson_name");
                    salesperson.setName(salespersonName != null ? salespersonName : "Ukendt sælger");
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
            o.order_price,
            c.name AS customer_name, 
            s.salesperson_id, 
            s.name AS salesperson_name 
        FROM orders o 
        LEFT JOIN customer c ON o.customer_number = c.phone_number 
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
                    order.setPrice(rs.getInt("order_price"));

                    // Set customer
                    Customer customer = new Customer();
                    customer.setName(rs.getString("customer_name"));
                    order.setCustomer(customer);

                    // Set salesperson
                    int salespersonId = rs.getInt("salesperson_id");
                    if (!rs.wasNull()) {
                        Salesperson salesperson = new Salesperson();
                        salesperson.setSalespersonId(salespersonId);
                        salesperson.setName(rs.getString("salesperson_name"));
                        order.setSalesperson(salesperson);
                    }

                    return order;
                }
            }
        }
        return null;
    }


}
