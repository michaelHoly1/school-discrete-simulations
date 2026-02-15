package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities;

public class Worker {

    private boolean isAtWarehouse;
    private boolean isAtWorkplace;
    private boolean isBusy;
    private int numberOfLastOccupiedWorkplace;
    private String group;
    private int id;
    private WorkerState state;
    private double totalFreeTime;
    private double lastFreeStartTime;
    
    
    


    public Worker(String group, int id) {

        this.isAtWarehouse = true;
        this.isAtWorkplace = false;
        this.isBusy = false;
        this.numberOfLastOccupiedWorkplace = -1;
        this.group = group;
        this.id = id;
        this.state = WorkerState.FREE_AT_WAREHOUSE;
        this.totalFreeTime = 0.0;
        this.lastFreeStartTime = 0.0;

    }
    

    public enum WorkerState {
        FREE_AT_WAREHOUSE("Free at warehouse"),
        FREE_AT_WORKPLACE("Free at workplace"),
        MOVING_TO_WAREHOUSE("Moving to warehouse"),
        MOVING_TO_WORKPLACE("Moving to workplace"),
        PREPARING_MATERIAL("Preparing material"),
        CUTTING("Cutting"),
        VARNISHING("Varnishing"),
        ASSEMBLING("Assembling"),
        EXPEDING_ORDER("Expeding order"),
        FORGING("Forging");

        private final String state;
        
        WorkerState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }

    }

    public boolean isAtWarehouse() {
        return isAtWarehouse;
    }

    public boolean isAtWorkplace() {
        return isAtWorkplace;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public int getLastOccupiedWorkplace() {
        return numberOfLastOccupiedWorkplace;
    }

    public String getGroup() {
        return group;
    }

    public int getId() {
        return id;
    }

    public WorkerState getState() {
        return state;
    }

    public double getWorkloadPercentage(double currentSimulationTime) {
        
        if (totalFreeTime == 0.0) {
            return 0.0;
        } else {
            double busyTime = currentSimulationTime - totalFreeTime;
        return (busyTime / currentSimulationTime) * 100.0;
        }
        
    }

    public void setAtWarehouse(boolean atWarehouse) {
        isAtWarehouse = atWarehouse;
    }

    public void setAtWorkplace(boolean atWorkplace) {
        isAtWorkplace = atWorkplace;
    }

    public void setBusy(boolean busy, double currentSimulationTime) {
        if (!this.isBusy && busy) {
            // From not busy -> busy -> calculate and reset free time
            if (lastFreeStartTime >= 0) {
                totalFreeTime += currentSimulationTime - lastFreeStartTime;
                lastFreeStartTime = -1; // reset
            }
        } else if (this.isBusy && !busy) {
            // From busy -> not busy -> set start time of free time
            lastFreeStartTime = currentSimulationTime;
        }
    
        this.isBusy = busy;
    }

    public void setLastOccupiedWorkplace(int numberOfLastWorkplace) {
        this.numberOfLastOccupiedWorkplace = numberOfLastWorkplace;
    }

    public void setState(WorkerState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Worker " + group + id;
    }








}
