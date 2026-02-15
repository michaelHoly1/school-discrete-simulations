package ds_sp2_holy.app.eventdriven.simulation;

import ds_sp2_holy.app.montecarlo.GeneralMonteCarloSimulation;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.generators.*;

import java.util.ArrayList;
import java.util.PriorityQueue;

public abstract class GeneralEventDrivenSimulation extends GeneralMonteCarloSimulation {

    protected double currentSimulationTime;
    protected double maximumSimulationTime;
    private PriorityQueue<GeneralEvent> eventQueue;
    private boolean pause;
    protected boolean simulationSlowdown;
    protected double slowdownRatio;
    private int replicationIndex;

    private final ArrayList<ISimDelegate> delegates = new ArrayList<>();
    

    

    public GeneralEventDrivenSimulation(SeedGenerator seedGenerator, double maximumSimulationTime) {
        super(seedGenerator);
        this.eventQueue = new PriorityQueue<>();
        this.maximumSimulationTime = maximumSimulationTime;
        this.simulationSlowdown = false;
        this.slowdownRatio = 0;
        
        
    }

    

    @Override
    protected void beforeReplication(int replicationIndex) {
        this.currentSimulationTime = 0;
        this.pause = false;
        this.simulationSlowdown = false;
        this.eventQueue.clear();
        this.turnOnSlowdown(this.slowdownRatio);
        
    }

    @Override
    protected void virtualReplication(int replicationIndex) {
        this.replicationIndex = replicationIndex;
        
        while (!this.eventQueue.isEmpty() && !this.stop) {

            while (this.pause) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            GeneralEvent currentEvent = this.eventQueue.poll();
            double eventExecutionTime = currentEvent.getTime();

            if (eventExecutionTime > this.maximumSimulationTime) {
                break;
            } else {
                this.currentSimulationTime = eventExecutionTime;
                currentEvent.execute();
                
            }
            

            

        }

    }

    @Override
    protected void afterReplication(int replicationIndex) {}
    

    public void pauseSimulation() {
        this.pause = true;
    }

    public void resumeSimulation() {
        this.pause = false;
    }

    public void turnOnSlowdown(double slowdownRatio) {
        if (slowdownRatio > 0) {

        if (!this.simulationSlowdown) {
            double nextSystemEventTime = this.currentSimulationTime + 1;
            this.addEvent(new SystemEvent(nextSystemEventTime, this));
        }


        this.simulationSlowdown = true;
        this.slowdownRatio = slowdownRatio;
        
        }
    }

    public void turnOffSlowdown() {
        this.simulationSlowdown = false;
        this.slowdownRatio = 0;
    }

    public void addEvent(GeneralEvent event) {
        if (event.getTime() < this.currentSimulationTime) {
            throw new IllegalArgumentException("Event finishTime cannot be less than the current simulation time");
        } else {
            this.eventQueue.add(event);
        }
    }

    public double getCurrentSimulationTime() {
        return currentSimulationTime;
    }

    public int getReplicationIndex() {
        return replicationIndex;
    }

    public boolean isSimulationSlowdown() {
        return simulationSlowdown;
    }

    public void registerDelegate(ISimDelegate delegate) {
        delegates.add(delegate);
    }

    public void refreshGUI(RefreshType type) {

        for (ISimDelegate delegate : delegates) {
            delegate.refresh(this, type);
        }
    }

}
