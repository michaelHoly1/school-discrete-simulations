package ds_sp2_holy.app.montecarlo;

import ds_sp2_holy.app.generators.SeedGenerator;

public abstract class GeneralMonteCarloSimulation {

    protected final SeedGenerator seedGenerator;
    protected boolean stop;
    protected int totalReplicationCount;

    public GeneralMonteCarloSimulation(SeedGenerator seedGenerator) {
        this.seedGenerator = seedGenerator;
    }

    public void stopSimulation() {
        this.stop = true;
    }
    
    
    public void simulate(int replicationCount) {
        this.totalReplicationCount = replicationCount;
        this.stop = false;
        beforeAllReplications();

        for (int i = 1; i <= replicationCount; i++) {
            
            if (stop) {
                break;
            }

            beforeReplication(i);
            virtualReplication(i);
            afterReplication(i);
        }

        afterAllReplications();
    }

    protected void beforeAllReplications() { }
    protected void afterAllReplications() { }

    protected abstract void beforeReplication(int replicationIndex);

    protected abstract void virtualReplication(int replicationIndex);

    protected abstract void afterReplication(int replicationIndex);

    public int getTotalReplicationCount() {
        return this.totalReplicationCount;
    }
}
