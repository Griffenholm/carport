package app.persistence;

import app.entities.Material;
import app.entities.Variant;
import app.exceptions.DatabaseException;

import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MaterialMapper
{

    private final ConnectionPool connectionPool;

    public MaterialMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<Variant> getMaterialVariantsByMaterialIdAndMinLength(int minLength, int materialId, ConnectionPool connectionPool) throws DatabaseException
    {
        List<Variant> variants = new ArrayList<>();
        String sql = "SELECT * FROM variant " +
                     "INNER JOIN material USING (material_id) " +
                     "WHERE material_id = ? AND length >= ?";

        try(Connection connection = connectionPool.getConnection())
        {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, materialId);
            ps.setInt(2, minLength);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                int variantId = rs.getInt("variant_id");
                int materialId1 = rs.getInt("material_id");
                int length = rs.getInt("length");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                double price = rs.getDouble("price");
                Material material = new Material(materialId1, name, price, unit);
                Variant variant = new Variant(variantId, length, material);
                variants.add(variant);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not get material variants and lengths from DB", e.getMessage());
        }
        return variants;
    }
}
