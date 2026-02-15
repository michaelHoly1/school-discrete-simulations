package ds_sp2_holy.app.eventdriven.simulation;


public interface ISimDelegate {


    public enum RefreshType {
        REFRESH_ENTITIES,
        REFRESH_TIME,
        REFRESH_GRAPH,
        REFRESH_FULLSPEED_STATISTICS,
        REFRESH_ANIMATION_STATISTICS;
        
        
    }

    void refresh(GeneralEventDrivenSimulation simulation, RefreshType type);
}
