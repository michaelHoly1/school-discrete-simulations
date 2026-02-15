package ds_sp2_holy.app.generators;

import java.util.Random;

public class ContinuousExponentialGenerator {

    private Random random;
    private double meanValue;

    public ContinuousExponentialGenerator(double meanValue, SeedGenerator seedGenerator) {
        this.meanValue = meanValue;
        this.random = new Random(seedGenerator.nextSeed());

    }

    public double nextDouble() {
        return -meanValue * Math.log(1 - random.nextDouble());
    }

    

}
