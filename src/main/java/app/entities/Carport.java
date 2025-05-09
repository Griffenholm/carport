package app.entities;

import java.util.List;

public class Carport {
 List<Material> materialList;
 int height;
 int length;
 int width;

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

    @Override
    public String toString() {
        return "Carport{" +
                "materialList=" + materialList +
                ", height=" + height +
                ", length=" + length +
                ", width=" + width +
                '}';
    }
}
