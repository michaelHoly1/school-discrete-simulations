package ds_sp2_holy.app.generators;

import java.util.Random;

public class ContinuousTriangularGenerator {

    private final Random random;
    private final double min;
    private final double max;
    private final double mode;

    public ContinuousTriangularGenerator(double min, double max, double mode, SeedGenerator seedGenerator) {
        if (min > mode || mode > max) {
            throw new IllegalArgumentException("Invalid parameters: min <= mode <= max must hold.");
        }

        this.min = min;
        this.max = max;
        this.mode = mode;
        this.random = new Random(seedGenerator.nextSeed());
    }

    public double nextDouble() {
        double u = random.nextDouble();
        double splitPoint = (mode - min) / (max - min);

        if (u < splitPoint) {
            return min + Math.sqrt(u * (max - min) * (mode - min));
        } else {
            return max - Math.sqrt((1 - u) * (max - min) * (max - mode));
        }
    }
}
