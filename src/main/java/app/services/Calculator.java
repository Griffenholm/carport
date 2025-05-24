package app.services;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;

import java.util.*;

public class Calculator {

    private static final int POSTS = 6;
    private static final int RAFTERS = 5;
    private static final int BEAMS = 5;
    private static final double MULTIPLIER = 5;

    private List<Orderline> orderlines = new ArrayList<>();
    private int width;
    private int length;
    private ConnectionPool connectionPool;
    private MaterialMapper materialMapper;

    public Calculator(int width, int length, ConnectionPool connectionPool) {
        this.width = width;
        this.length = length;
        this.connectionPool = connectionPool;
        this.materialMapper = new MaterialMapper(connectionPool);
    }

    public void calcCarport(Order order) throws DatabaseException {
        calcPost(order);
        calcBeams(order);
        calcRafters(order);
    }

    //Stolper
    public void calcPost(Order order) throws DatabaseException {
        int quantity = calcPostQuantity();

        List<Variant> variants = materialMapper.getMaterialVariantsByMaterialIdAndMinLength(1, POSTS, connectionPool);
        Variant variant = variants.get(0);
        Orderline orderline = new Orderline(0, order, variant, quantity, "Stolper nedgraves 90 cm. i jord", (variant.getMaterial().getPrice() * variant.getLength() * quantity));
        orderlines.add(orderline);
    }

    public int calcPostQuantity() {
        return 4 + (2 * ((length - 300) / 300));
    }

    //Remme
    public void calcBeams(Order order) throws DatabaseException {

        List<Variant> availableVariants = materialMapper.getMaterialVariantsByMaterialIdAndMinLength(300, BEAMS, connectionPool);

        Map<Variant, Integer> variantCountMap = calculateBeamVariantQuantities(length, availableVariants);

        //Convert the map to a list of orderlines
        for (Map.Entry<Variant, Integer> entry : variantCountMap.entrySet()) {
            Variant variant = entry.getKey();
            int quantity = entry.getValue();
            Orderline orderline = new Orderline(0, order, variant, quantity, "Remme til sider, sadles ned i stoper", (variant.getMaterial().getPrice() * variant.getLength() * quantity));
            orderlines.add(orderline);
        }
    }

    public Map<Variant, Integer> calculateBeamVariantQuantities(int length, List<Variant> availableVariants) {
        availableVariants.sort(Comparator.comparingInt(Variant::getLength).reversed());

        int remainingLength = length * 2;

        Map<Variant, Integer> variantCountMap = new HashMap<>();

        while (remainingLength > 0) {
            boolean found = false;
            for (Variant variant : availableVariants) {
                if (variant.getLength() <= remainingLength) {
                    variantCountMap.put(variant, variantCountMap.getOrDefault(variant, 0) + 1);
                    remainingLength -= variant.getLength();
                    found = true;
                    break;
                }
            }
            if (!found) {
                Variant shortest = availableVariants.get(availableVariants.size() - 1);
                variantCountMap.put(shortest, variantCountMap.getOrDefault(shortest, 0) + 1);
                remainingLength = 0;
            }
        }
        return variantCountMap;
    }

    //Spær
    public void calcRafters(Order order) throws DatabaseException {
        List<Variant> availableVariants = materialMapper.getMaterialVariantsByMaterialIdAndMinLength(300, RAFTERS, connectionPool);

        availableVariants.sort(Comparator.comparingInt(Variant::getLength));

        Map<Variant, Integer> variantQuantityMap = calculateRafterVariantQuantities(length, width, availableVariants);

        for (Map.Entry<Variant, Integer> entry : variantQuantityMap.entrySet()) {
            Variant variant = entry.getKey();
            int quantity = entry.getValue();

            Orderline orderline = new Orderline(0, order, variant, quantity, "Spær, montores på remme", (variant.getMaterial().getPrice() * quantity));
            orderlines.add(orderline);
        }
    }

    public Map<Variant, Integer> calculateRafterVariantQuantities(int length, int width, List<Variant> variants) throws DatabaseException {
        variants.sort(Comparator.comparingInt(Variant::getLength));

        Variant selectedVariant = null;
        for (Variant variant : variants) {
            if (variant.getLength() >= width) {
                selectedVariant = variant;
                break;
            }
        }

        if (selectedVariant == null) {
            throw new DatabaseException("No rafters available for the given width");
        }

        int numberOfRafters = (int) Math.floor((double) length / 60) + 1;

        if ((numberOfRafters - 1) * 60 < length) {
            numberOfRafters++;
        }

        Map<Variant, Integer> variantCountMap = new HashMap<>();
        variantCountMap.put(selectedVariant, numberOfRafters);

        return variantCountMap;
    }

    public List<Orderline> getOrderlines() {
        return orderlines;
    }

    public void calculateAndSetPrices(Order order) throws DatabaseException {
        this.calcCarport(order);
        double costPrice = this.getOrderlines().stream()
                .mapToDouble(Orderline::getOrderlinePrice)
                .sum();
        double price = costPrice * MULTIPLIER;
        order.setCostPrice(costPrice);
        order.setPrice(price);
    }
}
