package model;

public class WandWithDetails {
    private Wand wand;
    private WoodType woodType;
    private WandCore core;
    private Customer owner;  // Null if unsold

    // Constructor
    public WandWithDetails(Wand wand, WoodType woodType, WandCore core, Customer owner) {
        this.wand = wand;
        this.woodType = woodType;
        this.core = core;
        this.owner = owner;
    }

    public Wand getWand() {
        return wand;
    }

    public void setWand(Wand wand) {
        this.wand = wand;
    }

    public WoodType getWoodType() {
        return woodType;
    }

    public void setWoodType(WoodType woodType) {
        this.woodType = woodType;
    }

    public WandCore getCore() {
        return core;
    }

    public void setCore(WandCore core) {
        this.core = core;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return String.format("%s, Wood: %s, Core: %s",
                wand.toString(), woodType.getName(), core.getMaterial());
    }
}