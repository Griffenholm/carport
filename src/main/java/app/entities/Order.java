package app.entities;

import java.time.LocalDate;

public class Order {
    private int orderId;
    private int carportHeight;
    private int carportLength;
    private int carportWidth;
    private int shedLength;
    private int shedWidth;
    private double price;
    private String status;
    private LocalDate orderDate;
    private String svg;
    private LocalDate deliveryDate;
    private Carport carport;
    private Customer customer;
    private Salesperson salesperson;

    // Empty constructor (necessary for Javalin form-binding)
    public Order() {}

    public Order(int orderId, int carportHeight, int carportLength, int carportWidth, int shedLength, int shedWidth, int price, String status, LocalDate orderDate, String svg, LocalDate deliveryDate, Carport carport, Customer customer, Salesperson salesperson) {
        this.orderId = orderId;
        this.carportHeight = carportHeight;
        this.carportLength = carportLength;
        this.carportWidth = carportWidth;
        this.shedLength = shedLength;
        this.shedWidth = shedWidth;
        this.price = price;
        this.status = status;
        this.orderDate = orderDate;
        this.svg = svg;
        this.deliveryDate = deliveryDate;
        this.carport = carport;
        this.customer = customer;
        this.salesperson = salesperson;
    }

    public Order(int orderId, int carportHeight, int carportLength, int carportWidth, double price, String status, LocalDate deliveryDate, Customer customer) {
        this.orderId = orderId;
        this.carportHeight = carportHeight;
        this.carportLength = carportLength;
        this.carportWidth = carportWidth;
        this.price = price;
        this.status = status;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
    }

    public Order(int orderId, int carportLength, int carportWidth, double price, String status, LocalDate orderDate) {
        this.orderId = orderId;
        this.carportLength = carportLength;
        this.carportWidth = carportWidth;
        this.price = price;
        this.status = status;
        this.deliveryDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCarportHeight() {
        return carportHeight;
    }

    public void setCarportHeight(int carportHeight) {
        this.carportHeight = carportHeight;
    }

    public int getCarportLength() {
        return carportLength;
    }

    public void setCarportLength(int carportLength) {
        this.carportLength = carportLength;
    }

    public int getCarportWidth() {
        return carportWidth;
    }

    public void setCarportWidth(int carportWidth) {
        this.carportWidth = carportWidth;
    }

    public int getShedLength() {
        return shedLength;
    }

    public void setShedLength(int shedLength) {
        this.shedLength = shedLength;
    }

    public int getShedWidth() {
        return shedWidth;
    }

    public void setShedWidth(int shedWidth) {
        this.shedWidth = shedWidth;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Carport getCarport() {
        return carport;
    }

    public void setCarport(Carport carport) {
        this.carport = carport;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Salesperson getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(Salesperson salesperson) {
        this.salesperson = salesperson;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", carportHeight=" + carportHeight +
                ", carportLength=" + carportLength +
                ", carportWidth=" + carportWidth +
                ", shedLength=" + shedLength +
                ", shedWidth=" + shedWidth +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", order_date=" + orderDate +
                ", svg='" + svg + '\'' +
                ", deliveryDate=" + deliveryDate +
                ", carport=" + carport +
                ", customer=" + customer +
                ", salesperson=" + salesperson +
                '}';
    }
}
