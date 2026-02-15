package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.ProductionStage;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;


public class FinishForgingEvent extends GeneralEvent {

    private Order order;

    public FinishForgingEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        double readyForExpeditionTime = currentSimulationTime; // 2 mins
        

        Worker currentWorker = this.order.getCurrentWorker();
        currentWorker.setState(WorkerState.EXPEDING_ORDER);
        this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_EXPEDITION);
        this.order.setState(OrderState.IN_PROGRESS);
        this.order.setLastFinishedProductionStage(ProductionStage.FORGING);

    
        // Ready event for expedition
        
        ReadyForExpeditionEvent readyForExpeditionEvent = new ReadyForExpeditionEvent(readyForExpeditionTime, this.order, workshopSim);
        workshopSim.addEvent(readyForExpeditionEvent);


        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }
        
        

    }

}

