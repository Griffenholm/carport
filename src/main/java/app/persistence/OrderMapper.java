package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

            // Set a customer number (phone number)
            stmt.setInt(1, order.getCustomer().getPhoneNumber());
            stmt.setInt(2, order.getCarportHeight());
            stmt.setInt(3, order.getCarportWidth());
            stmt.setInt(4, order.getCarportLength());
            stmt.setObject(5, order.getShedWidth() != 0 ? order.getShedWidth() : null, java.sql.Types.INTEGER);
            stmt.setObject(6, order.getShedLength() != 0 ? order.getShedLength() : null, java.sql.Types.INTEGER);
            stmt.setString(7, order.getStatus());
            stmt.setDouble(8, order.getPrice());
            stmt.setDouble(9, order.getPrice()); // Assuming start price is the same as order price for now

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
}
