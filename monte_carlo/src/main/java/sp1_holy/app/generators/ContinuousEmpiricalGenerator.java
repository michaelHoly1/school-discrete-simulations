package sp1_holy.app.generators;
import java.util.Random;

public class ContinuousEmpiricalGenerator {
    private Random[] randoms;
    private Random probabilityRandom;
    private ContinuousEmpiricalTriple[] intervals;
    private static final double EPSILON = 1e-10;


    public ContinuousEmpiricalGenerator(ContinuousEmpiricalTriple[] intervals, SeedGenerator seedGenerator) {
        this.intervals = intervals;
        probabilityRandom = new Random(seedGenerator.nextSeed());
        this.randoms = new Random[intervals.length];
        double sum = 0;
        for (int i = 0; i < intervals.length; i++) {
            this.randoms[i] = new Random(seedGenerator.nextSeed());
            sum += intervals[i].getProbability();
        }

        if (Math.abs(sum - 1) > EPSILON) {
            throw new IllegalArgumentException("Sum of probabilities must be equal to 1");
        }
    }

    public double nextDouble() {
        double probability = this.probabilityRandom.nextDouble();
        double sum = 0;
        double randomNumber = 0;

        for (int i = 0; i < this.intervals.length; i++) {
            sum += this.intervals[i].getProbability();
            if (probability < sum + EPSILON) {
                randomNumber = this.randoms[i].nextDouble() * (this.intervals[i].getUMax() - this.intervals[i].getUMin()) + this.intervals[i].getUMin();
                break;
            }

        }

        return randomNumber;


    }
}
