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
            stmt.setDouble(9, order.getPrice()); // Assuming cost price is the same as order price for now
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
                SELECT\s
                    o.order_id,\s
                    o.status,\s
                    o.order_date,\s
                    c.name AS customer_name,\s
                    c.address AS customer_address,
                    c.zip AS customer_zip,
                    ci.city_name AS customer_city,
                    c.email AS customer_email,
                    c.phone_number AS customer_phone_number,
                    s.salesperson_id,\s
                    s.name AS salesperson_name
                FROM orders o\s
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
                SELECT\s
                           o.order_id,\s
                           o.status,\s
                           o.order_date,\s
                           o.carport_height,
                           o.carport_width,
                           o.carport_length,
                           o.order_price,
                           c.name AS customer_name,\s
                           c.address AS customer_address,
                           c.zip AS customer_zip,
                           c.email AS customer_email,
                           c.phone_number AS customer_phone,
                           ci.city_name AS customer_city,
                           s.salesperson_id,\s
                           s.name AS salesperson_name\s
                       FROM orders o\s
                       LEFT JOIN customer c ON o.customer_number = c.phone_number\s
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
                    order.setPrice(rs.getInt("order_price"));

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
                        order.setSalesperson(salesperson);
                    }

                    return order;
                }
            }
        }
        return null;
    }



    public List<Order> getAllOrders() throws DatabaseException
    {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders INNER JOIN customer USING (customer_number) INNER JOIN salesperson USING (salesperson_id)";
        try(
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
        )
        {
            while(rs.next()){
                int customerNumber = rs.getInt("phone_number");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String email = rs.getString("email");
                int zip = rs.getInt("zip");
                int orderId = rs.getInt("order_id");
                int carportHeight = rs.getInt("carport_height");
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                String status = rs.getString("status");
                int price = rs.getInt("order_price");
                LocalDate deliveryDate = rs.getDate("delivery_date").toLocalDate();
                int salespersonId = rs.getInt("salesperson_id");
                Customer customer = new Customer(name, address, zip, "Nowhere", customerNumber, email);
                Order order = new Order(orderId, carportHeight, carportLength, carportWidth, price, status, deliveryDate, customer);
                orderList.add(order);
            }
         }
        catch (SQLException e){
            throw new DatabaseException("Could not get orders from database", e.getMessage());
        }
        return orderList;
    }

    public List<Orderline> getOrderlineListByOrderId(int orderId) throws DatabaseException
    {
     List<Orderline> OrderlineList = new ArrayList<>();
     String sql = "SELECT * FROM bill_of_materials_view WHERE order_id = ?";

     try(
             Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
        )
        {
         ps.setInt(1, orderId);
         ResultSet rs = ps.executeQuery();
         while (rs.next())
            {
                // Order
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                String status = rs.getString("status");
                double orderPrice = rs.getDouble("order_price");
                LocalDate orderDate = rs.getDate("order_date").toLocalDate();
                Order order = new Order (orderId, carportWidth, carportLength, orderPrice, status, orderDate);

                // Material
                int materialId = rs.getInt("material_id");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                double materialPrice = rs.getDouble("price");
                Material material = new Material(materialId, name, "", materialPrice, unit);

                // Variant
                int variantId = rs.getInt("variant_id");
                int length = rs.getInt("length");
                Variant variant = new Variant(variantId, length, material);

                // Orderline
                int orderlineId = rs.getInt("orderline_id");
                int quantity = rs.getInt("quantity");
                double orderlinePrice = rs.getDouble("orderline_price");
                Orderline orderline = new Orderline (orderlineId, order, variant, quantity, "", orderlinePrice);
                OrderlineList.add(orderline);
            }
        }
     catch (SQLException e){
            throw new DatabaseException("Could not get orderline from database", e.getMessage());
        }
        return OrderlineList;

    }

    public void insertOrderline(List<Orderline> orderlines) throws DatabaseException
    {
        String sql = "INSERT INTO orderline (order_id, variant_id, quantity, build_description, orderline_price)" +
                     "VALUES (?, ?, ?, ?, ?)";

        try(Connection connection = connectionPool.getConnection())
        {
            for(Orderline orderline : orderlines){

                try(PreparedStatement ps = connection.prepareStatement(sql))
                {
                    ps.setInt(1, orderline.getOrder().getOrderId());
                    ps.setInt(2, orderline.getVariant().getVariantId());
                    ps.setInt(3, orderline.getQuantity());
                    ps.setString(4, orderline.getBuildDescription());
                    ps.setDouble(5, orderline.getOrderlinePrice());
                    ps.executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Could not insert orderline into database", e.getMessage());
        }
    }

    public void updateOrderPrice(int orderId, double newPrice) throws SQLException {
        String sql = "UPDATE orders SET order_price = ? WHERE order_id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }

}
