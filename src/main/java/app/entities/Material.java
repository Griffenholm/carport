package app.entities;

public class Material {
    int materialId;
    String name;
    double price;
    String unit;
    int quantity;
    int width;
    int height;

    public Material(int materialId, String name, double price, String unit, int quantity, int width, int height) {
        this.materialId = materialId;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.quantity = quantity;
        this.width = width;
        this.height = height;
    }

    public Material(int materialId, String name, double price, String unit) {
        this.materialId = materialId;
        this.name = name;
        this.price = price;
        this.unit = unit;
    }

    public Material(int materialId, String name, String s, double materialPrice, String unit) {
        this.materialId = materialId;
        this.name = name;
        this.price = materialPrice;
        this.unit = unit;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Material{" +
                "materialId=" + materialId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
