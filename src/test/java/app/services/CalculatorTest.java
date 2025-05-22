package app.services;

import app.persistence.ConnectionPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import app.entities.*;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest
{

    private static final String USER = "postgres";
    private static final String PASSWORD = System.getenv("password"); // We have used environment variables for security. Click main -> Edit Configurations -> Environment Variables -> Add password and ip
    private static final String URL = "jdbc:postgresql://" + System.getenv("ip") + ":5432/%s?currentSchema=public";
    private static final String DB = "fog";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    @BeforeAll
    static void setup()
    {

    }

    @Test
    void calcPostQuantity()
    {
        Calculator calculator = new Calculator(600, 910, connectionPool);
        assertEquals(8, calculator.calcPostQuantity());

    }

    @Test
    void testCalculateBeamVariantQuantities() {
        Variant v600 = new Variant(600);
        Variant v360 = new Variant(360);
        Variant v300 = new Variant(300);

        List<Variant> variants = Arrays.asList(v600, v360, v300);

        Calculator calc = new Calculator(300, 700, connectionPool); // Or whatever your class is

        Map<Variant, Integer> result = calc.calculateBeamVariantQuantities(500, variants); // 500 * 2 = 1000

        // Assertions
        assertEquals(3, result.size());
        assertEquals(1, result.get(v600));
        assertEquals(1, result.get(v360));
        assertEquals(1, result.get(v300));

        // Optional: check total length
        int totalCovered = result.entrySet().stream()
                .mapToInt(e -> e.getKey().getLength() * e.getValue())
                .sum();
        assertTrue(totalCovered >= 1000);
    }
}