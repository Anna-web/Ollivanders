package model;

public class WandCore {
    private int coreId;
    private String material;
    private String description;
    private int dangerLevel;

    public WandCore(int coreId, String material, String description, int dangerLevel) {
        this.coreId = coreId;
        this.material = material;
        this.description = description;
        this.dangerLevel = dangerLevel;
    }

    public int getCoreId() {
        return coreId;
    }

    public void setCoreId(int coreId) {
        this.coreId = coreId;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(int dangerLevel) {
        this.dangerLevel = dangerLevel;
    }
}