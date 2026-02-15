package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.ArrayList;

import ds_sp2_holy.app.eventdriven.simulation.GeneralEventDrivenSimulation;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.generators.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.Statistics;
import ds_sp2_holy.app.eventdriven.simulation.WeightedStatistics;

public class FurnitureWorkshopSimulation extends GeneralEventDrivenSimulation {

    protected double nextUpdateTimeForAnimation;
    protected double animationUpdateInterval = 1800; // 30 minutes -> seconds
    protected int numberOfFinishedOrders = 0;
    
    protected ArrayList<Order> activeOrders = new ArrayList<Order>();
    protected ArrayList<Worker> allWorkersA = new ArrayList<Worker>();
    protected ArrayList<Worker> allWorkersB = new ArrayList<Worker>();
    protected ArrayList<Worker> allWorkersC = new ArrayList<Worker>();
    protected ArrayList<Workplace> allWorkplaces = new ArrayList<Workplace>();

    private int numberOfWorkersA;
    private int numberOfWorkersB;
    private int numberOfWorkersC;
    protected int nextAvailableWorkplaceNumber = 0;
    protected int nextAvailableOrderID = 0;
    protected LinkedList<Order> unstartedOrders = new LinkedList<Order>();
    protected LinkedList<Order> ordersWaitingForVarnishing = new LinkedList<Order>();
    protected LinkedList<Order> ordersWaitingForAssembly = new LinkedList<Order>();
    protected LinkedList<Order> ordersWaitingForForging = new LinkedList<Order>();
    protected LinkedList<Order> ordersWaitingForExpedition = new LinkedList<Order>();
    protected LinkedList<Worker> freeWorkersA = new LinkedList<Worker>();
    protected LinkedList<Worker> freeWorkersB = new LinkedList<Worker>();   
    protected LinkedList<Worker> freeWorkersC = new LinkedList<Worker>();
    protected PriorityQueue<Workplace> freeWorkplaces = new PriorityQueue<Workplace>();

    protected Statistics averageOrderFinishTimeReplicationStatistic = new Statistics();
    protected WeightedStatistics averageCountOfUnstartedOrdersReplicationStatistic = new WeightedStatistics();
    protected double lastUnstartedOrdersQueueChangeTime;


    protected Statistics averageOrderFinishTimeGlobalStatistic = new Statistics();
    protected Statistics averageCountOfUnstartedOrdersGlobalStatistic = new Statistics();
    protected ArrayList<Statistics> averageWorkloadWorkersAGlobalStatistics = new ArrayList<Statistics>();
    protected ArrayList<Statistics> averageWorkloadWorkersBGlobalStatistics = new ArrayList<Statistics>();
    protected ArrayList<Statistics> averageWorkloadWorkersCGlobalStatistics = new ArrayList<Statistics>();
    protected Statistics averageWorkloadGroupAGlobalStatistic = new Statistics();
    protected Statistics averageWorkloadGroupBGlobalStatistic = new Statistics();
    protected Statistics averageWorkloadGroupCGlobalStatistic = new Statistics();

    protected final ContinuousExponentialGenerator orderFlowGenerator = new ContinuousExponentialGenerator(1800, seedGenerator); // <30 minutes -> seconds
    protected final DiscreteEmpiricalGenerator orderTypeGenerator; // 0,5 table; 0,15 chair; 0,35 closet.

    protected final ContinuousTriangularGenerator moveTimeBetweenWarehouseWorkplaceGenerator = new ContinuousTriangularGenerator(60, 480, 120, seedGenerator); // <60;480;120) -> seconds
    protected final ContinuousTriangularGenerator moveTimeBetweenWorkplacesGenerator = new ContinuousTriangularGenerator(120, 500, 150,  seedGenerator); // <120;500;150) -> seconds
    protected final ContinuousTriangularGenerator timeToPrepareMaterialGenerator = new ContinuousTriangularGenerator(300, 900, 500, seedGenerator); // <300;900;500) -> seconds

    protected final ContinuousEmpiricalGenerator tableCuttingTimeGenerator;
    protected final ContinuousUniformGenerator tableVarnishingTimeGenerator = new ContinuousUniformGenerator(12000, 36600, seedGenerator); // <200;610) minutes -> seconds
    protected final ContinuousUniformGenerator tableAssemblyTimeGenerator = new ContinuousUniformGenerator(1800, 3600, seedGenerator); // <30;60) minutes -> seconds

    protected final ContinuousUniformGenerator chairCuttingTimeGenerator = new ContinuousUniformGenerator(720, 960, seedGenerator); // <12;16) minutes -> seconds
    protected final ContinuousUniformGenerator chairVarnishingTimeGenerator = new ContinuousUniformGenerator(12600, 32400, seedGenerator); // <210;540) minutes -> seconds
    protected final ContinuousUniformGenerator chairAssemblyTimeGenerator = new ContinuousUniformGenerator(840, 1440, seedGenerator); // <14;24) minutes -> seconds

    protected final ContinuousUniformGenerator closetCuttingTimeGenerator = new ContinuousUniformGenerator(900, 4800, seedGenerator); // <15;80) minutes -> seconds
    protected final ContinuousUniformGenerator closetVarnishingTimeGenerator = new ContinuousUniformGenerator(36000, 42000, seedGenerator); // <600;700) minutes -> seconds
    protected final ContinuousUniformGenerator closetAssemblyTimeGenerator = new ContinuousUniformGenerator(2100, 4500, seedGenerator); // <35;75) minutes -> seconds
    protected final ContinuousUniformGenerator closetForgingTimeGenerator = new ContinuousUniformGenerator(900, 1500, seedGenerator); // <15;25) minutes -> seconds

    public FurnitureWorkshopSimulation(SeedGenerator seedGenerator, double maxSimulationTime, int numberOfWorkersA, int numberOfWorkersB, int numberOfWorkersC) {
        super(seedGenerator, maxSimulationTime);
        this.numberOfWorkersA = numberOfWorkersA;
        this.numberOfWorkersB = numberOfWorkersB;
        this.numberOfWorkersC = numberOfWorkersC;

        ContinuousEmpiricalTriple[] intervals = new ContinuousEmpiricalTriple[2];
        intervals[0] = new ContinuousEmpiricalTriple(600, 1500, 0.6); // <10;25) minutes -> seconds
        intervals[1] = new ContinuousEmpiricalTriple(1500, 3000, 0.4); // <25;50) minutes -> seconds

        this.tableCuttingTimeGenerator = new ContinuousEmpiricalGenerator(intervals, seedGenerator);

        DiscreteEmpiricalTriple[] orderTypeIntervals = new DiscreteEmpiricalTriple[3];
        orderTypeIntervals[0] = new DiscreteEmpiricalTriple(1, 2, 0.5);
        orderTypeIntervals[1] = new DiscreteEmpiricalTriple(2, 3, 0.15);
        orderTypeIntervals[2] = new DiscreteEmpiricalTriple(3, 4, 0.35);

        this.orderTypeGenerator = new DiscreteEmpiricalGenerator(orderTypeIntervals, seedGenerator);
    }


    @Override
    protected void beforeAllReplications() {
        super.beforeAllReplications();
        this.resetAllGlobalStatistics();
    }

    @Override
    protected void beforeReplication(int replicationIndex) {
        super.beforeReplication(replicationIndex);
        this.resetAllReplicationStatistics();
        this.resetAllQueues();
        this.nextAvailableWorkplaceNumber = 0;
        this.nextAvailableOrderID = 0;
        this.lastUnstartedOrdersQueueChangeTime = 0;
        this.nextUpdateTimeForAnimation = 1800; // 30 minutes -> seconds
        this.numberOfFinishedOrders = 0;

        OrderArrivalEvent orderArrivalEvent = new OrderArrivalEvent(0, this);
        this.addEvent(orderArrivalEvent);
        
    }

    @Override
    protected void afterReplication(int replicationIndex) {
        super.afterReplication(replicationIndex);
        

        if (!this.stop) {
            this.averageOrderFinishTimeGlobalStatistic.addData(this.averageOrderFinishTimeReplicationStatistic.getMean());
            this.averageCountOfUnstartedOrdersGlobalStatistic.addData(this.averageCountOfUnstartedOrdersReplicationStatistic.getMean());
            this.updateWorkloadStatisticsForAllWorkers();
            this.averageWorkloadGroupAGlobalStatistic.addData(this.getAverageWorkloadPercentageForWholeGroup("A"));
            this.averageWorkloadGroupBGlobalStatistic.addData(this.getAverageWorkloadPercentageForWholeGroup("B"));
            this.averageWorkloadGroupCGlobalStatistic.addData(this.getAverageWorkloadPercentageForWholeGroup("C"));
            this.refreshGUI(RefreshType.REFRESH_FULLSPEED_STATISTICS);
            this.refreshGUI(RefreshType.REFRESH_GRAPH);
            
        }
        
        
    }

    
    @Override
    protected void afterAllReplications() {

        if (!this.simulationSlowdown) {
            this.refreshGUI(RefreshType.REFRESH_FULLSPEED_STATISTICS);
        } else {
            this.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }
        
    }

    public double getAverageWorkloadPercentageForWholeGroup(String group) {

        double sumOfWorkloadPercentages = 0;

        if (group.equals("A")) {
            for (int i = 0; i < this.numberOfWorkersA; i++) {
                sumOfWorkloadPercentages += this.allWorkersA.get(i).getWorkloadPercentage(currentSimulationTime);
            }
            return sumOfWorkloadPercentages / this.numberOfWorkersA;

        } else if (group.equals("B")) {
            for (int i = 0; i < this.numberOfWorkersB; i++) {
                sumOfWorkloadPercentages += this.allWorkersB.get(i).getWorkloadPercentage(currentSimulationTime);
            }
            return sumOfWorkloadPercentages / this.numberOfWorkersB;
        } else if (group.equals("C")) {
            for (int i = 0; i < this.numberOfWorkersC; i++) {
                sumOfWorkloadPercentages += this.allWorkersC.get(i).getWorkloadPercentage(currentSimulationTime);
            }
            return sumOfWorkloadPercentages / this.numberOfWorkersC;
        } else {
            return 0;
        }

    }

    private void updateWorkloadStatisticsForAllWorkers() {

        for (int i = 0; i < this.numberOfWorkersA; i++) {
            this.averageWorkloadWorkersAGlobalStatistics.get(i).addData(this.allWorkersA.get(i).getWorkloadPercentage(currentSimulationTime));
        }

        for (int i = 0; i < this.numberOfWorkersB; i++) {
            this.averageWorkloadWorkersBGlobalStatistics.get(i).addData(this.allWorkersB.get(i).getWorkloadPercentage(currentSimulationTime));
        }

        for (int i = 0; i < this.numberOfWorkersC; i++) {
            this.averageWorkloadWorkersCGlobalStatistics.get(i).addData(this.allWorkersC.get(i).getWorkloadPercentage(currentSimulationTime));
        }
    }

    private void resetAllQueues() {
        this.unstartedOrders.clear();
        this.ordersWaitingForVarnishing.clear();
        this.ordersWaitingForAssembly.clear();
        this.ordersWaitingForForging.clear();
        this.ordersWaitingForExpedition.clear();
        this.freeWorkersA.clear();
        this.freeWorkersB.clear();
        this.freeWorkersC.clear();
        this.freeWorkplaces.clear();
        this.allWorkplaces.clear();
        this.allWorkersA.clear();
        this.allWorkersB.clear();
        this.allWorkersC.clear();
        this.activeOrders.clear();


        for (int i = 0; i < this.numberOfWorkersA; i++) {
            Worker worker = new Worker("A", i);
            this.allWorkersA.add(worker);
            this.freeWorkersA.add(worker);
        }

        for (int i = 0; i < this.numberOfWorkersB; i++) {
            Worker worker = new Worker("B", i);
            this.allWorkersB.add(worker);
            this.freeWorkersB.add(worker);
        }

        for (int i = 0; i < this.numberOfWorkersC; i++) {
            Worker worker = new Worker("C", i);
            this.allWorkersC.add(worker);
            this.freeWorkersC.add(worker);
        }
    }

    private void resetAllGlobalStatistics() {

        this.averageOrderFinishTimeGlobalStatistic.reset();
        this.averageCountOfUnstartedOrdersGlobalStatistic.reset();
       
        this.averageWorkloadGroupAGlobalStatistic.reset();
        this.averageWorkloadGroupBGlobalStatistic.reset();
        this.averageWorkloadGroupCGlobalStatistic.reset();

        this.averageWorkloadWorkersAGlobalStatistics.clear();
        this.averageWorkloadWorkersBGlobalStatistics.clear();
        this.averageWorkloadWorkersCGlobalStatistics.clear();

        for (int i = 0; i < this.numberOfWorkersA; i++) {
            Statistics workerAGlobalStatistic = new Statistics();
            this.averageWorkloadWorkersAGlobalStatistics.add(workerAGlobalStatistic);
        }

        for (int i = 0; i < this.numberOfWorkersB; i++) {
            Statistics workerBGlobalStatistic = new Statistics();
            this.averageWorkloadWorkersBGlobalStatistics.add(workerBGlobalStatistic);
        }

        for (int i = 0; i < this.numberOfWorkersC; i++) {
            Statistics workerCGlobalStatistic = new Statistics();
            this.averageWorkloadWorkersCGlobalStatistics.add(workerCGlobalStatistic);
        }
    }

    private void resetAllReplicationStatistics() {
        this.averageOrderFinishTimeReplicationStatistic.reset();
        this.averageCountOfUnstartedOrdersReplicationStatistic.reset();
        
    }

    

    public ArrayList<Order> getActiveOrders() {
        return activeOrders;
    }

    public ArrayList<Worker> getAllWorkersA() {
        return allWorkersA;
    }

    public ArrayList<Worker> getAllWorkersB() {
        return allWorkersB;
    }

    public ArrayList<Worker> getAllWorkersC() {
        return allWorkersC;
    }

    public ArrayList<Workplace> getAllWorkplaces() {
        return allWorkplaces;
    }

    public int getUnstartedOrdersSize() {
        return unstartedOrders.size();
    }

    public int getOrdersWaitingForVarnishingSize() {
        return ordersWaitingForVarnishing.size();
    }

    public int getOrdersWaitingForAssemblySize() {
        return ordersWaitingForAssembly.size();
    }

    public int getOrdersWaitingForForgingSize() {
        return ordersWaitingForForging.size();
    }

    public int getOrdersWaitingForExpeditionSize() {
        return ordersWaitingForExpedition.size();
    }

    public int getFreeWorkersASize() {
        return freeWorkersA.size();
    }

    public int getFreeWorkersBSize() {
        return freeWorkersB.size();
    }

    public int getFreeWorkersCSize() {
        return freeWorkersC.size();
    }

    public int getFreeWorkplacesSize() {
        return freeWorkplaces.size();
    }

    public Statistics getAverageOrderFinishTimeReplicationStatistics() {
        return averageOrderFinishTimeReplicationStatistic;
    }

    public WeightedStatistics getAverageCountOfUnstartedOrdersReplicationStatistics() {
        return averageCountOfUnstartedOrdersReplicationStatistic;
    }

    public Statistics getAverageOrderFinishTimeGlobalStatistics() {
        return averageOrderFinishTimeGlobalStatistic;
    }

    public Statistics getAverageCountOfUnstartedOrdersGlobalStatistics() {
        return averageCountOfUnstartedOrdersGlobalStatistic;
    }

    public ArrayList<Statistics> getAverageWorkloadWorkersAGlobalStatistics() {
        return averageWorkloadWorkersAGlobalStatistics;
    }

    public ArrayList<Statistics> getAverageWorkloadWorkersBGlobalStatistics() {
        return averageWorkloadWorkersBGlobalStatistics;
    }

    public ArrayList<Statistics> getAverageWorkloadWorkersCGlobalStatistics() {
        return averageWorkloadWorkersCGlobalStatistics;
    }

    public Statistics getAverageWorkloadGroupAGlobalStatistics() {
        return averageWorkloadGroupAGlobalStatistic;
    }

    public Statistics getAverageWorkloadGroupBGlobalStatistics() {
        return averageWorkloadGroupBGlobalStatistic;
    }

    public Statistics getAverageWorkloadGroupCGlobalStatistics() {
        return averageWorkloadGroupCGlobalStatistic;
    }

    public int getNumberOfFinishedOrders() {
        return numberOfFinishedOrders;
    }



    

}
