package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.ProductionStage;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Workplace.WorkplaceState;


public class FinalizationOfOrderEvent extends GeneralEvent {

    private Order order;

    public FinalizationOfOrderEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();

        workshopSim.numberOfFinishedOrders++;
        
        this.order.setLastFinishedProductionStage(ProductionStage.FINISHED);
        this.order.setState(OrderState.FINISHED);

        Workplace assignedWorkplace = this.order.getAssignedWorkplace();
        assignedWorkplace.setState(WorkplaceState.FREE);
        this.order.setAssignedWorkplace(null);

        workshopSim.freeWorkplaces.add(assignedWorkplace);
        
        if (workshopSim.isSimulationSlowdown()) {
            workshopSim.activeOrders.remove(this.order);
        }
        

        double orderFinishTime = currentSimulationTime - this.order.getSimulationTimeAtStart();
        
        workshopSim.averageOrderFinishTimeReplicationStatistic.addData(orderFinishTime);
        

        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        } 
    
    }


}
