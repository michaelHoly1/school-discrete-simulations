package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.ProductionStage;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;


public class FinishMaterialPrepEvent extends GeneralEvent {

    private Order order;

    public FinishMaterialPrepEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        this.order.setLastFinishedProductionStage(ProductionStage.MATERIAL_PREP);
        this.order.getCurrentWorker().setState(WorkerState.MOVING_TO_WORKPLACE);
        
        double moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
        MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, this.order, workshopSim);
        workshopSim.addEvent(moveEvent);


        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }

    }

}
