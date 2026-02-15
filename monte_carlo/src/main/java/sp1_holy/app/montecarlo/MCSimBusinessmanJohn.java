package sp1_holy.app.montecarlo;
import java.util.function.BiConsumer;

import sp1_holy.app.generators.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MCSimBusinessmanJohn extends GeneralMonteCarloSimulation {

    protected  DiscreteUniformGenerator shockAbsorbersGenerator = new DiscreteUniformGenerator(50, 100, seedGenerator);
    protected  DiscreteUniformGenerator brakePlatesGenerator = new DiscreteUniformGenerator(60, 250, seedGenerator);
    protected  DiscreteEmpiricalGenerator headlightsGenerator;
    protected  ContinuousUniformGenerator probabilityGenerator = new ContinuousUniformGenerator(0, 100, seedGenerator);
    protected final ContinuousUniformGenerator supplier1First10Weeks = new ContinuousUniformGenerator(10.0, 70.0, seedGenerator);
    protected final ContinuousUniformGenerator supplier1After10Weeks = new ContinuousUniformGenerator(30.0, 95.0, seedGenerator);
    protected final ContinuousEmpiricalGenerator supplier2First15Weeks;
    protected final ContinuousEmpiricalGenerator supplier2After15Weeks;

    protected final double dailyShockAbsorbersStoringCost = 0.2;
    protected final double dailyBrakePlatesStoringCost = 0.3;
    protected final double dailyHeadlightsStoringCost = 0.25;
    protected final double failureToDeliverPenalty = 0.3;
    protected static final double EPSILON = 1e-10;
    protected int shockAbsorbersCount;
    protected int brakePlatesCount;
    protected int headlightsCount;
    protected double totalCosts;
    protected double propability;
    protected double deliveryPropability;
    protected boolean successfulDelivery;

    protected BiConsumer<Integer, Double> graphUpdater;
    protected int graphUpdateInterval;
    protected int lastIgnoredIndex;

    private CustomOrder[] weeklyOrders;

    public MCSimBusinessmanJohn(SeedGenerator seedGenerator, String fileName) {
        super(seedGenerator);
        DiscreteEmpiricalTriple[] intervals = new DiscreteEmpiricalTriple[4];
        intervals[0] = new DiscreteEmpiricalTriple(30, 60, 0.2);
        intervals[1] = new DiscreteEmpiricalTriple(60, 100, 0.4);
        intervals[2] = new DiscreteEmpiricalTriple(100, 140, 0.3);
        intervals[3] = new DiscreteEmpiricalTriple(140, 160, 0.1);
        this.headlightsGenerator = new DiscreteEmpiricalGenerator(intervals, seedGenerator);

        ContinuousEmpiricalTriple[] intervalsFirst15Weeks = new ContinuousEmpiricalTriple[5];
        ContinuousEmpiricalTriple[] intervalsAfter15Weeks = new ContinuousEmpiricalTriple[5];
        intervalsFirst15Weeks[0] = new ContinuousEmpiricalTriple(5.0, 10.0, 0.4);
        intervalsFirst15Weeks[1] = new ContinuousEmpiricalTriple(10.0, 50.0, 0.3);
        intervalsFirst15Weeks[2] = new ContinuousEmpiricalTriple(50.0, 70.0, 0.2);
        intervalsFirst15Weeks[3] = new ContinuousEmpiricalTriple(70.0, 80.0, 0.06);
        intervalsFirst15Weeks[4] = new ContinuousEmpiricalTriple(80.0, 95.0, 0.04);

        intervalsAfter15Weeks[0] = new ContinuousEmpiricalTriple(5.0, 10.0, 0.2);
        intervalsAfter15Weeks[1] = new ContinuousEmpiricalTriple(10.0, 50.0, 0.4);
        intervalsAfter15Weeks[2] = new ContinuousEmpiricalTriple(50.0, 70.0, 0.3);
        intervalsAfter15Weeks[3] = new ContinuousEmpiricalTriple(70.0, 80.0, 0.06);
        intervalsAfter15Weeks[4] = new ContinuousEmpiricalTriple(80.0, 95.0, 0.04);

        this.supplier2First15Weeks = new ContinuousEmpiricalGenerator(intervalsFirst15Weeks, seedGenerator);
        this.supplier2After15Weeks = new ContinuousEmpiricalGenerator(intervalsAfter15Weeks, seedGenerator);

        this.graphUpdater = null;

        this.weeklyOrders = new CustomOrder[30];
        try {
            this.loadWeeklyOrders(fileName);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading weekly orders");
        }

    }

    public void setGraphUpdater(BiConsumer<Integer, Double> graphUpdater) {
        this.graphUpdater = graphUpdater;
    }

    private void loadWeeklyOrders(String fileName) throws IOException {
        String path = "C:\\Users\\holya\\OneDrive\\Počítač\\Škola\\2.semester\\DS\\Semestrálky\\SP1\\monte_carlo\\src\\main\\resources\\";
        //getClass().getClassLoader().getResourceAsStream(fileName);
        File file = new File(path + fileName);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int week = 0;
        line = br.readLine(); // header line

        while ((line = br.readLine()) != null && week < weeklyOrders.length) {
            String[] values = line.split(";");
            int shockAbsorbersCount = Integer.parseInt(values[0]);
            int brakePlatesCount = Integer.parseInt(values[1]);
            int headlightsCount = Integer.parseInt(values[2]);
            int supplier = Integer.parseInt(values[3]);
            this.weeklyOrders[week] = new CustomOrder(shockAbsorbersCount, brakePlatesCount, headlightsCount, supplier);
            week++;
        }

        br.close();
        
    }

    
    @Override
    protected void beforeAllReplications() {

        this.totalCosts = 0;
        this.graphUpdateInterval = Math.max(1, (int)Math.floor(totalReplicationCount / 1000));
        this.lastIgnoredIndex = (int)Math.floor(this.totalReplicationCount * 0.15);
    }

    @Override
    protected void beforeReplication(int replicationIndex) {
        
        this.shockAbsorbersCount = 0;
        this.brakePlatesCount = 0;
        this.headlightsCount = 0;

    }

    @Override
    protected void virtualReplication(int replicationIndex) {

        for (int week = 1; week <= 30; week++) {

            this.propability = this.probabilityGenerator.nextDouble();
            this.successfulDelivery = false;

            if (this.weeklyOrders[week - 1].getSupplier() == 1) {

                if (week <= 10) {
                    this.deliveryPropability = this.supplier1First10Weeks.nextDouble();
                } else {
                    this.deliveryPropability = this.supplier1After10Weeks.nextDouble();
                }

            } else {

                if (week <= 15) {
                    this.deliveryPropability = this.supplier2First15Weeks.nextDouble();
                } else {
                    this.deliveryPropability = this.supplier2After15Weeks.nextDouble();
                }

            }

            if (this.propability < this.deliveryPropability + EPSILON) {
                this.successfulDelivery = true;
            }

            for (int day = 1; day <= 7; day++) {
                
                if (day == 1 && successfulDelivery) {

                    this.shockAbsorbersCount += this.weeklyOrders[week - 1].getShockAbsorbersCount();
                    this.brakePlatesCount += this.weeklyOrders[week - 1].getBrakePlatesCount();
                    this.headlightsCount += this.weeklyOrders[week - 1].getHeadlightsCount();
                    

                } else if (day == 5) {

                    this.shockAbsorbersCount -= this.shockAbsorbersGenerator.nextInt();
                    this.brakePlatesCount -= this.brakePlatesGenerator.nextInt();
                    this.headlightsCount -= this.headlightsGenerator.nextInt();

                    if (this.shockAbsorbersCount < 0) {
                        this.totalCosts += this.failureToDeliverPenalty * Math.abs(this.shockAbsorbersCount);
                        this.shockAbsorbersCount = 0;
                    } 

                    if (this.brakePlatesCount < 0) {
                        this.totalCosts += this.failureToDeliverPenalty * Math.abs(this.brakePlatesCount);
                        this.brakePlatesCount = 0;
                    }

                    if (this.headlightsCount < 0) {
                        this.totalCosts += this.failureToDeliverPenalty * Math.abs(this.headlightsCount);
                        this.headlightsCount = 0;
                    }

                }


                
                this.totalCosts += this.shockAbsorbersCount * this.dailyShockAbsorbersStoringCost;
                this.totalCosts += this.brakePlatesCount * this.dailyBrakePlatesStoringCost;
                this.totalCosts += this.headlightsCount * this.dailyHeadlightsStoringCost;

                if (this.totalReplicationCount == 1 && this.graphUpdater != null) {
                    this.graphUpdater.accept(((week - 1) * 7) + day, this.totalCosts);
                }
            
            }
            }

    }

    @Override
    protected void afterReplication(int replicationIndex) {

        if (this.totalReplicationCount != 1 ) {

            if (this.graphUpdater != null && replicationIndex % this.graphUpdateInterval == 0 && replicationIndex > this.lastIgnoredIndex) {
                this.graphUpdater.accept(replicationIndex, this.totalCosts / replicationIndex);
            }

        }

    }

}
