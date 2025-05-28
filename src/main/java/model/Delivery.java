package model;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class Delivery {
    private int deliveryId;
    private String deliveryDate;
    private String supplierName;
    private String receivedBy;
    private String notes;
    private List<DeliveryItem> items;

    public Delivery(){

    }

    public Delivery(int deliveryId, String deliveryDate, String supplierName, String receivedBy, String notes, List<DeliveryItem> items) {
        this.deliveryId = deliveryId;
        this.deliveryDate = deliveryDate;
        this.supplierName = supplierName;
        this.receivedBy = receivedBy;
        this.notes = notes;
        this.items = items;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<DeliveryItem> getItems() {
        return items;
    }

    public void setItems(List<DeliveryItem> items) {
        this.items = items;
    }
}