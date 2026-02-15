package ds_sp2_holy.app.eventdriven.simulation;

import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;

public class SystemEvent extends GeneralEvent {

    public SystemEvent(double time,  GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        
    }

    @Override
    public void execute() {

        if (this.simulation.simulationSlowdown && this.simulation.slowdownRatio > 0) {

            Long sleepTime = (long) (1000 / this.simulation.slowdownRatio);

            try {
                Thread.sleep((long) sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            double nextSystemEventTime = this.simulation.currentSimulationTime + 1;
            this.simulation.addEvent(new SystemEvent(nextSystemEventTime, this.simulation));

            this.simulation.refreshGUI(RefreshType.REFRESH_TIME);
        }

        
    }

}
