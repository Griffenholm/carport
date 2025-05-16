package app.entities;

public class Orderline
{

    private int orderlineId;
    private Order order;
    private Variant variant;
    private int quantity;
    private String buildDescription;
    private double orderlinePrice;

    public Orderline(int orderlineId, Order order, Variant variant, int quantity, String buildDescription, double orderlinePrice)
    {
        this.orderlineId = orderlineId;
        this.order = order;
        this.variant = variant;
        this.quantity = quantity;
        this.buildDescription = buildDescription;
        this.orderlinePrice = orderlinePrice;
    }

    public int getOrderlineId()
    {
        return orderlineId;
    }

    public void setOrderlineId(int orderlineId)
    {
        this.orderlineId = orderlineId;
    }

    public Order getOrder()
    {
        return order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    public Variant getVariant()
    {
        return variant;
    }

    public void setVariant(Variant variant)
    {
        this.variant = variant;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public String getBuildDescription()
    {
        return buildDescription;
    }

    public void setBuildDescription(String buildDescription)
    {
        this.buildDescription = buildDescription;
    }

    public double getOrderlinePrice()
    {
        return orderlinePrice;
    }

    public void setOrderlinePrice(double orderlinePrice)
    {
        this.orderlinePrice = orderlinePrice;
    }
}
