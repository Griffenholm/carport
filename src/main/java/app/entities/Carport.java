package app.entities;

import java.util.List;

public class Carport {
    private List<Material> materialList;
    private int height;
    private int length;
    private int width;
    private String roofType;
    private String roofMaterial;
    private int roofSlope;

    public Carport(int height, int length, int width) {
        this.height = height;
        this.length = length;
        this.width = width;
    }

    public List<Material> getMaterialList() {
        return materialList;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setMaterialList(List<Material> materialList) {
        this.materialList = materialList;
    }

    public String getRoofType() {
        return roofType;
    }

    public void setRoofType(String roofType) {
        this.roofType = roofType;
    }

    public String getRoofMaterial() {
        return roofMaterial;
    }

    public void setRoofMaterial(String roofMaterial) {
        this.roofMaterial = roofMaterial;
    }

    public int getRoofSlope() {
        return roofSlope;
    }

    public void setRoofSlope(int roofSlope) {
        this.roofSlope = roofSlope;
    }

    @Override
    public String toString() {
        return "Carport{" +
                "materialList=" + materialList +
                ", height=" + height +
                ", length=" + length +
                ", width=" + width +
                ", roofType='" + roofType + '\'' +
                ", roofMaterial='" + roofMaterial + '\'' +
                ", roofSlope=" + roofSlope +
                '}';
    }
}
