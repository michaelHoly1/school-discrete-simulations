package sp1_holy.app.montecarlo;

public class CustomOrder {

    private int shockAbsorbersCount;
    private int brakePlatesCount;
    private int headlightsCount;
    private int supplier;

    public CustomOrder(int shockAbsorbersCount, int brakePlatesCount, int headlightsCount, int supplier) {
        this.shockAbsorbersCount = shockAbsorbersCount;
        this.brakePlatesCount = brakePlatesCount;
        this.headlightsCount = headlightsCount;
        this.supplier = supplier;
    }

    public int getShockAbsorbersCount() {
        return shockAbsorbersCount;
    }

    public int getBrakePlatesCount() {
        return brakePlatesCount;
    }

    public int getHeadlightsCount() {
        return headlightsCount;
    }

    public int getSupplier() {
        return supplier;
    }

}
