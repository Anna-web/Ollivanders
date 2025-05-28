package model;

import java.util.Date;

public class Wand {
    private int id;
    private int woodId;
    private int coreId;
    private double length;
    private String flexibility;
    private String condition;  // new, used, etc.
    private double price;
    private String status; // in_stock, sold, etc.
    private String specialFeatures;
    private String notes;
    private String productionDate;

    public Wand() {
    }

    public Wand(int woodId, int coreId, double length, String flexibility,
                String condition, String specialFeatures, double price,
                String status, String notes){
        this.woodId = woodId;
        this.coreId = coreId;
        this.length = length;
        this.flexibility = flexibility;
        this.condition = condition;
        this.price = price;
        this.status = status;
        this.specialFeatures = specialFeatures;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWoodId() {
        return woodId;
    }

    public void setWoodId(int woodId) {
        this.woodId = woodId;
    }

    public int getCoreId() {
        return coreId;
    }

    public void setCoreId(int coreId) {
        this.coreId = coreId;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getFlexibility() {
        return flexibility;
    }

    public void setFlexibility(String flexibility) {
        this.flexibility = flexibility;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecialFeatures() {
        return specialFeatures;
    }

    public void setSpecialFeatures(String specialFeatures) {
        this.specialFeatures = specialFeatures;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    @Override
    public String toString() {
        return String.format("Wand #%d (%.1f inches, %s condition)", id, length, condition);
    }
}