package model;

import java.time.LocalDate;

public class Purchase {
    private int purchaseId;
    private int wandId;
    private int customerId;
    private String saleDate;
    private double salePrice;
    private String paymentMethod;  // galleons, gringotts, credit

    public Purchase(){

    }

    // Constructors
    public Purchase(int wandId, int customerId, double salePrice) {
        this.wandId = wandId;
        this.customerId = customerId;
        this.salePrice = salePrice;
    }

    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

    public int getWandId() {
        return wandId;
    }

    public void setWandId(int wandId) {
        this.wandId = wandId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Getters & Setters
    // ... (implement for all fields)

    @Override
    public String toString() {
        return String.format("Sale #%d: %.2f galleons", purchaseId, salePrice);
    }
}