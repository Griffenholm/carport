package app.persistence;

import app.entities.City;
import app.entities.Customer;
import app.entities.Salesperson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {

    private final ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void saveUser(Customer customer) throws SQLException {
        String sql = """
    INSERT INTO customer (name, address, zip, phone_number, email) 
    VALUES (?, ?, ?, ?, ?)
    """;
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getAddress());
            ps.setInt(3, customer.getZip());
            ps.setInt(4, customer.getPhoneNumber());
            ps.setString(5, customer.getEmail());
            ps.executeUpdate();
        }
    }

    public Salesperson assignSalesperson() throws SQLException {
        String sql = """
                SELECT s.salesperson_id, s.name, s.email, s.password, s.is_admin,
                       COALESCE(COUNT(DISTINCT o.order_id), 0) as active_orders
                FROM salesperson s
                LEFT JOIN orders o ON s.salesperson_id = o.salesperson_id
                WHERE o.status = 'Ny ordre' OR o.status IS NULL
                GROUP BY s.salesperson_id, s.name, s.email, s.password, s.is_admin
                ORDER BY active_orders ASC
                LIMIT 1;
            """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int salespersonId = rs.getInt("salesperson_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                boolean isAdmin = rs.getBoolean("is_admin");

                // Return salesperson with the fewest customers
                return new Salesperson(salespersonId, name, email, password, isAdmin);
            }
        }

        throw new IllegalStateException("Ingen sælgere tilgængelige.");
    }

    public City getCityByZip(int zip) throws SQLException {
        String sql = "SELECT zip, city_name FROM city WHERE zip = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, zip);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new City(rs.getInt("zip"), rs.getString("city_name"));
                }
            }
        }
        return null;
    }


}
