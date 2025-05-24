package app.entities;

public class Variant {
    int variantId;
    int orderlineId;
    int materialId;
    int length;
    Material material;

    public Variant(int variantId, int length, Material material)
    {
        this.variantId = variantId;
        this.length = length;
        this.material = material;
    }

    //Contructor for testing, I only need the length in the object
    public Variant (int length)
    {
        this.length = length;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getOrderlineId() {
        return orderlineId;
    }

    public void setOrderlineId(int orderlineId) {
        this.orderlineId = orderlineId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @Override
    public String toString() {
        return "Variant{" +
                "variantId=" + variantId +
                ", orderlineId=" + orderlineId +
                ", materialId=" + materialId +
                ", length=" + length +
                ", material=" + material +
                '}';
    }
}
