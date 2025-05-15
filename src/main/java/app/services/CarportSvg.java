package app.services;

public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;

    public CarportSvg(int width, int length) {
        this.width = width;
        this.length = length;
        carportSvg = new Svg(0, 0, "0 0 " + width + " " + length, width + "px", length + "px");

        // Add frame
        carportSvg.addRectangle(0, 0, length, width, "stroke-width:2px; stroke:#000000; fill: #ffffff");

        // Add beams and rafters
        addBeams();
        addRafters();
    }

    private void addBeams() {
        carportSvg.addRectangle(0, 35, 4.5, width, "stroke-width:2px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0, length - 35, 4.5, width, "stroke-width:2px; stroke:#000000; fill: #ffffff");
    }

    private void addRafters() {
        for (double i = 0; i < width; i += 55) {
            carportSvg.addRectangle(i, 0, length, 4.5, "stroke-width:2px; stroke:#000000; fill: #ffffff");
        }
    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}