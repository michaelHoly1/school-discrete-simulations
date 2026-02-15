package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;

public class FinishVarnishingEvent extends GeneralEvent{

    private Order order;

    public FinishVarnishingEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        double moveTime;
        double finishTime;

        
        Worker oldWorkerC = this.order.getCurrentWorker();
        this.order.setCurrentWorker(null);
        this.order.setLastFinishedProductionStage(ProductionStage.VARNISHING);
        
        // Primarily start the next order waiting for forging, otherwise start next order waiting for varnishing if there are any in queue or return the worker to queue
        if (workshopSim.ordersWaitingForForging.size() > 0) {
            
            Order nextForgingOrder = workshopSim.ordersWaitingForForging.poll();
            nextForgingOrder.setCurrentWorker(oldWorkerC);
            nextForgingOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
            nextForgingOrder.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);

            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextForgingOrder, workshopSim);
            workshopSim.addEvent(moveEvent);
                
        } else if (workshopSim.ordersWaitingForVarnishing.size() > 0) {

            Order nextVarnishingOrder = workshopSim.ordersWaitingForVarnishing.poll();
            nextVarnishingOrder.setCurrentWorker(oldWorkerC);
            nextVarnishingOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
            nextVarnishingOrder.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);

            moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
            MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextVarnishingOrder, workshopSim);
            workshopSim.addEvent(moveEvent);
             
        // Free workerC if there are no forging/varnishing orders    
        } else {
            
            oldWorkerC.setBusy(false, currentSimulationTime);
            oldWorkerC.setState(WorkerState.FREE_AT_WORKPLACE);
            workshopSim.freeWorkersC.add(oldWorkerC);

        }    
            
        // If there is worker from B that is free, Start the next production stage for current order
        if (workshopSim.freeWorkersB.size() > 0) {

            Worker assignedWorkerB = workshopSim.freeWorkersB.poll();
            this.order.setCurrentWorker(assignedWorkerB);
            assignedWorkerB.setBusy(true, currentSimulationTime);
            
            if (assignedWorkerB.getLastOccupiedWorkplace() == this.order.getAssignedWorkplace().getId()) {
                
                switch(this.order.getOrderType()) {
                    case TABLE:
                        finishTime = workshopSim.tableAssemblyTimeGenerator.nextDouble();
                        break;
                    case CHAIR:
                        finishTime = workshopSim.chairAssemblyTimeGenerator.nextDouble();
                        break;
                    case CLOSET:
                        finishTime = workshopSim.closetAssemblyTimeGenerator.nextDouble();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid order type");
                }

                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_ASSEMBLING);
                assignedWorkerB.setState(WorkerState.ASSEMBLING);
                FinishAssemblyEvent finishAssemblyEvent = new FinishAssemblyEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishAssemblyEvent);

            // Worker is at warehouse at the start of simulation
            } else if (assignedWorkerB.isAtWarehouse()) {

                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
                assignedWorkerB.setState(WorkerState.MOVING_TO_WORKPLACE);
                moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
                MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, this.order, workshopSim);
                workshopSim.addEvent(moveEvent);

            // Worker is not at warehouse, he is at different workplace than the current one
            } else {

                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
                assignedWorkerB.setState(WorkerState.MOVING_TO_WORKPLACE);
                moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
                MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, this.order, workshopSim);
                workshopSim.addEvent(moveEvent);

            }

        // There are no free workers from group B, put the order in the queue for next production stage
        } else {
            this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
            this.order.setState(OrderState.IN_QUEUE_FOR_ASSEMBLY);
            workshopSim.ordersWaitingForAssembly.add(this.order);
        }


        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }
    }
}
