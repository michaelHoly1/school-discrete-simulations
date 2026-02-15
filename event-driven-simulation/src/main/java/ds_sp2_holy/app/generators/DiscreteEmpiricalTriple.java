package ds_sp2_holy.app.generators;


public class DiscreteEmpiricalTriple {
    private int uMin;
    private int uMax;
    private double probability;

    public DiscreteEmpiricalTriple(int uMin, int uMax, double probability) {
        this.uMin = uMin;
        this.uMax = uMax;
        this.probability = probability;
    }

    public int getUMin() {
        return uMin;
    }

    public int getUMax() {
        return uMax;
    }

    public double getProbability() {
        return probability;
    }
}
