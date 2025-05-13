package app.persistence;

import app.entities.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderMapper {

    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void saveOrder(Order order) throws SQLException {
        String sql = """
        INSERT INTO orders (customer_number, carport_height, carport_width, carport_length, shed_width, shed_length, status, order_price, start_price, salesperson_id) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set customer number (phone number)
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


}
