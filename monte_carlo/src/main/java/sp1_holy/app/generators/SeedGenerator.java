package sp1_holy.app.generators;
import java.util.Random;

public class SeedGenerator {
    
    private Random random;

    public SeedGenerator(Long seed) {
        if (seed != null) {
            random = new Random(seed);
        } else {
            random = new Random();
        }
    }

    public int nextSeed() {
        return random.nextInt();
    }
}
