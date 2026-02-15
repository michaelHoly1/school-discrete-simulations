package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.ProductionStage;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderState;

public class FinishAssemblyEvent extends GeneralEvent {

    private Order order;

    public FinishAssemblyEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        double moveTime;
        double finishTime;

        
        Worker oldWorkerB = this.order.getCurrentWorker();
        this.order.setCurrentWorker(null);
        this.order.setLastFinishedProductionStage(ProductionStage.ASSEMBLY);
        
        // Start of next assembly order if there are any in queue
        if (workshopSim.ordersWaitingForAssembly.size() > 0) {
            Order nextAssemblyOrder = workshopSim.ordersWaitingForAssembly.poll();
            nextAssemblyOrder.setCurrentWorker(oldWorkerB);
            nextAssemblyOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerB.setState(WorkerState.MOVING_TO_WORKPLACE);
            nextAssemblyOrder.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);

            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextAssemblyOrder, workshopSim);
            workshopSim.addEvent(moveEvent);
            
        // Free workerB if there are no orders waiting for assembly
        } else {
            oldWorkerB.setBusy(false, currentSimulationTime);
            oldWorkerB.setState(WorkerState.FREE_AT_WORKPLACE);
            workshopSim.freeWorkersB.add(oldWorkerB);
        }

        // If there is a free worker from group C and current order is type CLOSET, start the next production stage for current order = forging
        if (workshopSim.freeWorkersC.size() > 0 && this.order.getOrderType() == OrderType.CLOSET) {
            
            Worker assignedWorkerC = workshopSim.freeWorkersC.poll();
            this.order.setCurrentWorker(assignedWorkerC);
            assignedWorkerC.setBusy(true, currentSimulationTime);

            if (assignedWorkerC.getLastOccupiedWorkplace() == this.order.getAssignedWorkplace().getId()) {
                    
                assignedWorkerC.setState(WorkerState.FORGING);
                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_FORGING);
                finishTime = workshopSim.closetForgingTimeGenerator.nextDouble();
                FinishForgingEvent finishForgingEvent = new FinishForgingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishForgingEvent);

            } else if (assignedWorkerC.isAtWarehouse()) {
                
                assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
                moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
                MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, this.order, workshopSim);
                workshopSim.addEvent(moveEvent);

            } else {

                assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
                moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
                MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, this.order, workshopSim);
                workshopSim.addEvent(moveEvent);
                
            }

        // If there is not a free worker from C and current order is type CLOSET, add this order to the queue for orders waiting for forging
        } else if (this.order.getOrderType() == OrderType.CLOSET) {
            this.order.setState(OrderState.IN_QUEUE_FOR_FORGING);
            this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
            workshopSim.ordersWaitingForForging.add(this.order);

        // If there is a free worker from C and current order is not type CLOSET = Ready order for expedition
        } else if (workshopSim.freeWorkersC.size() > 0 && this.order.getOrderType() != OrderType.CLOSET) {
            
            Worker assignedWorkerC = workshopSim.freeWorkersC.poll();
            assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
            this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
            this.order.setCurrentWorker(assignedWorkerC);
            assignedWorkerC.setBusy(true, currentSimulationTime);
            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, this.order, workshopSim);
            
            workshopSim.addEvent(moveEvent);

        } else if (this.order.getOrderType() == OrderType.CLOSET) {
            this.order.setState(OrderState.IN_QUEUE_FOR_FORGING);
            this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
            workshopSim.ordersWaitingForForging.add(this.order);
            
        } else if (this.order.getOrderType() != OrderType.CLOSET) {
            this.order.setState(OrderState.IN_QUEUE_FOR_EXPEDITION);
            this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
            workshopSim.ordersWaitingForExpedition.add(this.order);
        }
        

        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }
        
        
        

    }

}
