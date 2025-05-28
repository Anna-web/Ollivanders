package model;

public class InventoryItem {
    private int itemId;
    private String itemType; // 'wood' or 'core'
    private int materialId;
    private String materialName;
    private int quantity;
    private String lastUpdated;

    public InventoryItem(String itemType, int materialId, String materialName, int quantity, String lastUpdated) {
        this.itemType = itemType;
        this.materialId = materialId;
        this.materialName = materialName;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
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

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}