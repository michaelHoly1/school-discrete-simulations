package sp1_holy.app.generators;
import java.util.Random;

public class DiscreteUniformGenerator {
    private Random random;
    private int uMin;
    private int uMax;

    public DiscreteUniformGenerator(int uMin, int uMax, SeedGenerator seedGenerator) {
        this.uMin = uMin;
        this.uMax = uMax;
        this.random = new Random(seedGenerator.nextSeed());
        
        
    }

    public int nextInt() {
        return this.random.nextInt(uMax - uMin + 1) + uMin;
    }
}
