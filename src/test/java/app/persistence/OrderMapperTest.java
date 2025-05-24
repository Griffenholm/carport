package app.persistence;

import app.entities.Customer;
import app.entities.Order;
import app.entities.Salesperson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private static final String USER = "postgres";
    private static final String PASSWORD = System.getenv("password"); // We have used environment variables for security. Click main -> Edit Configurations -> Environment Variables -> Add password and ip
    private static final String URL = "jdbc:postgresql://" + System.getenv("ip") + ":5432/%s?currentSchema=public";
    private static final String DB = "fog";

    private static ConnectionPool connectionPool;
    private static OrderMapper orderMapper;

    @BeforeAll
    static void setup() {
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
        orderMapper = new OrderMapper(connectionPool);
    }

    @Test
    void getOrderById_shouldReturnOrderWithCustomerAndSalesperson() throws SQLException {
        // Arrange
        int testOrderId = 1; // Make sure this order exists with valid customer and salesperson

        // Act
        Order order = orderMapper.getOrderById(testOrderId);

        // Assert
        assertNotNull(order, "Order should not be null");
        assertEquals(testOrderId, order.getOrderId(), "Order ID should match the expected value");

        // Customer assertions
        Customer customer = order.getCustomer();
        assertNotNull(customer, "Customer should not be null");
        assertNotNull(customer.getName(), "Customer name should not be null");
        assertTrue(customer.getPhoneNumber() > 0, "Customer phone number should be a valid number");

        // Salesperson assertions
        Salesperson salesperson = order.getSalesperson();
        assertNotNull(salesperson, "Salesperson should not be null");
        assertNotNull(salesperson.getName(), "Salesperson name should not be null");
        assertNotNull(salesperson.getEmail(), "Salesperson email should not be null");
        assertTrue(salesperson.getPhoneNumber() > 0, "Salesperson phone number should be a valid number");
    }
}
