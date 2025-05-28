package model;

public class DeliveryItem {
    private String itemType; // 'wood' or 'core'
    private int materialId;
    private int quantity;

    public DeliveryItem(String itemType, int materialId, int quantity) {
        this.itemType = itemType;
        this.materialId = materialId;
        this.quantity = quantity;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}