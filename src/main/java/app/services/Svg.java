package app.services;

public class Svg {

    private static final String SVG_TEMPLATE = "<svg version=\"1.1\"\n" +
            "     x=\"%d\" y=\"%d\"\n" +
            "     viewBox=\"%s\"  width=\"%s\" \n" +
            "     height=\"%s\" preserveAspectRatio=\"xMinYMin\">";

    private static final String SVG_ARROW_DEFS = "<defs>\n" +
            "        <marker id=\"beginArrow\" markerWidth=\"12\" markerHeight=\"12\" refX=\"0\" refY=\"6\" orient=\"auto\">\n" +
            "            <path d=\"M0,6 L12,0 L12,12 L0,6\" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "        <marker id=\"endArrow\" markerWidth=\"12\" markerHeight=\"12\" refX=\"12\" refY=\"6\" orient=\"auto\">\n" +
            "            <path d=\"M0,0 L12,6 L0,12 L0,0 \" style=\"fill: #000000;\" />\n" +
            "        </marker>\n" +
            "    </defs>";

    private static final String SVG_RECT_TEMPLATE = "<rect x=\"%.0f\" y=\"%.0f\" height=\"%.0f\" width=\"%.0f\" style=\"%s\" />";

    private StringBuilder svg = new StringBuilder();

    public Svg(int x, int y, String viewBox, String width, String height) {
        svg.append(String.format(SVG_TEMPLATE, x, y, viewBox, width, height));
        svg.append(SVG_ARROW_DEFS);
    }

    public void addRectangle(double x, double y, double height, double width, String style) {
        svg.append(String.format(SVG_RECT_TEMPLATE, x, y, height, width, style));
    }

    public void addLine(int x1, int y1, int x2, int y2, String style) {
    }

    public void addArrow(int x1, int y1, int x2, int y2, String style) {
        // Call addLine with the style and add the arrow markers
    }

    public void addText(int x, int y, int rotation, String text) {
    }

    public void addSvg(Svg innerSvg) {
        svg.append(innerSvg.toString());
    }

    @Override
    public String toString() {
        return svg.append("</svg>").toString();
    }
}
