package app.persistence;

import app.entities.Carport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CarportMapper {

    private final ConnectionPool connectionPool;

    public CarportMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void saveCarport(Carport carport) {
    }

    public List<String> getRoofMaterials() throws SQLException {
        List<String> roofMaterials = new ArrayList<>();
        String sql = "SELECT name FROM material WHERE name ILIKE '%tagplade%'";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roofMaterials.add(rs.getString("name"));
            }
        }
        return roofMaterials;
    }
}
