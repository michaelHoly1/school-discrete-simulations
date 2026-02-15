package ds_sp2_holy.app.eventdriven.simulation;

public abstract class GeneralEvent implements Comparable<GeneralEvent> {

    private double eventTime;
    protected GeneralEventDrivenSimulation simulation;

    public GeneralEvent(double time, GeneralEventDrivenSimulation simulation) {
        this.eventTime = time;
        this.simulation = simulation;
    }

    public abstract void execute();

    public double getTime() {
        return eventTime;
    }

    @Override
    public int compareTo(GeneralEvent other) {
        return Double.compare(this.eventTime, other.eventTime);
    }

}
