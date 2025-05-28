package model;

public class WoodType {
    private int woodId;
    private String name;
    private String rarity;
    private String description;

    public WoodType(int woodId, String name, String rarity, String description) {
        this.woodId = woodId;
        this.name = name;
        this.rarity = rarity;
        this.description = description;
    }

    public int getWoodId() {
        return woodId;
    }

    public void setWoodId(int woodId) {
        this.woodId = woodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}