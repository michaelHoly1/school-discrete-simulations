package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.ProductionStage;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;

public class ReadyForExpeditionEvent extends GeneralEvent {

    private Order order;

    public ReadyForExpeditionEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        double moveTime;
        this.order.setLastFinishedProductionStage(ProductionStage.EXPEDITION);
        Worker oldWorkerC = this.order.getCurrentWorker();
        this.order.setCurrentWorker(null);
        
        
        // Primarily start the next order waiting for forging if there are any in queue
        if (workshopSim.ordersWaitingForExpedition.size() > 0) {

            Order nextExpeditionOrder = workshopSim.ordersWaitingForExpedition.poll();
            nextExpeditionOrder.setCurrentWorker(oldWorkerC);
            nextExpeditionOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
            nextExpeditionOrder.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);


            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextExpeditionOrder, workshopSim);
            workshopSim.addEvent(moveEvent);

        } else if (workshopSim.ordersWaitingForForging.size() > 0) {

            Order nextForgingOrder = workshopSim.ordersWaitingForForging.poll();
            nextForgingOrder.setCurrentWorker(oldWorkerC);
            nextForgingOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
            nextForgingOrder.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);


            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextForgingOrder, workshopSim);
            workshopSim.addEvent(moveEvent);
                
        // Otherwise start next order waiting for varnishing if there are any in queue or return the worker to queue    
        } else if (workshopSim.ordersWaitingForVarnishing.size() > 0) {

            Order nextVarnishingOrder = workshopSim.ordersWaitingForVarnishing.poll();
            nextVarnishingOrder.setCurrentWorker(oldWorkerC);
            nextVarnishingOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
            nextVarnishingOrder.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);

            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextVarnishingOrder, workshopSim);
            workshopSim.addEvent(moveEvent);

        } else {
                
                oldWorkerC.setBusy(false, currentSimulationTime);
                oldWorkerC.setState(WorkerState.FREE_AT_WORKPLACE);
                workshopSim.freeWorkersC.add(oldWorkerC);
        }
        
        
        FinalizationOfOrderEvent finalizationEvent = new FinalizationOfOrderEvent(currentSimulationTime, this.order, workshopSim);
        workshopSim.addEvent(finalizationEvent);

        // Update GUI
        workshopSim.refreshGUI(ISimDelegate.RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(ISimDelegate.RefreshType.REFRESH_ANIMATION_STATISTICS);
        }

}
}
