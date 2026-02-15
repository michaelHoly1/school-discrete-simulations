package sp1_holy.app;
import sp1_holy.app.montecarlo.*;

import java.util.function.BiConsumer;

import sp1_holy.app.generators.*;

public class BusinessmanJohnApplication {

    private SeedGenerator seedGenerator;
    private MCSimBusinessmanJohn strategyA;
    private MCSimBusinessmanJohn strategyB;
    private MCSimBusinessmanJohn strategyC;
    private MCSimBusinessmanJohn strategyD;
    private MCSimBusinessmanJohn strategyCustom;
    private MCSimBusinessmanJohn currentStrategy;

    public BusinessmanJohnApplication (Long seed) {
        this.seedGenerator = new SeedGenerator(seed);
        this.strategyA = new MCSimBusinessmanJohn(seedGenerator, "strategyA.csv");
        this.strategyB = new MCSimBusinessmanJohn(seedGenerator, "strategyB.csv");
        this.strategyC = new MCSimBusinessmanJohn(seedGenerator, "strategyC.csv");
        this.strategyD = new MCSimBusinessmanJohn(seedGenerator, "strategyD.csv");
        this.strategyCustom = new MCSimBusinessmanJohn(seedGenerator, "customOrdersMeanValues.csv");
        this.currentStrategy = null;
    }

    public void setGraphUpdater(BiConsumer<Integer, Double> graphUpdater) {
        this.strategyA.setGraphUpdater(graphUpdater);
        this.strategyB.setGraphUpdater(graphUpdater);
        this.strategyC.setGraphUpdater(graphUpdater);
        this.strategyD.setGraphUpdater(graphUpdater);
        this.strategyCustom.setGraphUpdater(graphUpdater);
    }

    public void runSimulation(int simulationType, int replicationCount) {

        switch (simulationType) {
            case 1:
                this.currentStrategy = strategyA;
                this.strategyA.simulate(replicationCount);
                break;
            case 2:
                this.currentStrategy = strategyB;
                this.strategyB.simulate(replicationCount);
                break;
            case 3:
                this.currentStrategy = strategyC;
                this.strategyC.simulate(replicationCount);
                break;
            case 4:
                this.currentStrategy = strategyD;
                this.strategyD.simulate(replicationCount);
                break;
            case 5:
                this.currentStrategy = strategyCustom;
                this.strategyCustom.simulate(replicationCount);
                break;
            default:
                throw new IllegalArgumentException("Invalid simulation type");
        }

    }

    public void stopSimulation() {
        this.currentStrategy.stopSimulation();
    }

}
