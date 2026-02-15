package ds_sp2_holy.app.generators;

import java.util.Random;

public class ContinuousUniformGenerator {

    private Random random;
    private double uMin;
    private double uMax;

    public ContinuousUniformGenerator(double uMin, double uMax, SeedGenerator seedGenerator) {
        this.uMin = uMin;
        this.uMax = uMax;
        this.random = new Random(seedGenerator.nextSeed());
    }

    public double nextDouble() {
        return this.random.nextDouble() * (uMax - uMin) + uMin;
    }
}
