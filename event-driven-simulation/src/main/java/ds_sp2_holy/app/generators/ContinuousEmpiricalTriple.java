package ds_sp2_holy.app.generators;


public class ContinuousEmpiricalTriple {

    private double uMin;
    private double uMax;
    private double probability;

    public ContinuousEmpiricalTriple(double uMin, double uMax, double probability) {
        this.uMin = uMin;
        this.uMax = uMax;
        this.probability = probability;
    }

    public double getUMin() {
        return uMin;
    }

    public double getUMax() {
        return uMax;
    }

    public double getProbability() {
        return probability;
    }
}
