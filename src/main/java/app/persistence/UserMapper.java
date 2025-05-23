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
        // Check if customer exists
        Customer existingCustomer = findCustomerByPhone(customer.getPhoneNumber());

        if (existingCustomer == null) {
            // If the customer does not exist, create new
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
        } else {
            // If customer already exists, update their data
            String updateSql = """
                UPDATE customer 
                SET name = ?, address = ?, zip = ?, email = ?
                WHERE phone_number = ?
                """;
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, customer.getName());
                ps.setString(2, customer.getAddress());
                ps.setInt(3, customer.getZip());
                ps.setString(4, customer.getEmail());
                ps.setInt(5, customer.getPhoneNumber());
                ps.executeUpdate();
            }
        }
    }


    public Salesperson assignSalesperson() throws SQLException {
        String sql = """
                SELECT s.salesperson_id, s.name, s.email, s.password, s.is_admin,
                       COALESCE(COUNT(o.order_id), 0) AS active_orders
                FROM salesperson s
                LEFT JOIN orders o\s
                    ON s.salesperson_id = o.salesperson_id\s
                    AND o.status = 'Ny ordre'
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

                // Return salesperson with the fewest 'Ny ordre'
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

    public Customer findCustomerByPhone(int phoneNumber) throws SQLException {
        String sql = "SELECT name, address, zip, phone_number, email FROM customer WHERE phone_number = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phoneNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String address = rs.getString("address");
                    int zip = rs.getInt("zip");
                    String email = rs.getString("email");
                    return new Customer(name, address, zip, "", phoneNumber, email);
                }
            }
        }
        return null;
    }

    public Salesperson getSalespersonByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT * FROM salesperson WHERE email = ? AND password = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Salesperson(
                        rs.getInt("salesperson_id"),
                        rs.getString("name"),
                        email,
                        password,
                        rs.getBoolean("is_admin"),
                        rs.getInt("phone_number")
                );
            }
        }
        return null;
    }
}
