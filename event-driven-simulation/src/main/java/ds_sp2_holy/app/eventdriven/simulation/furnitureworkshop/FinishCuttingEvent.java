package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.ProductionStage;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Workplace.WorkplaceState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderState;

public class FinishCuttingEvent extends GeneralEvent {

    private Order order;

    public FinishCuttingEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        double moveTime;
        double finishTime;

        
        Worker oldWorkerA = this.order.getCurrentWorker();
        this.order.setCurrentWorker(null);
        this.order.setLastFinishedProductionStage(ProductionStage.CUTTING);
        
        // Start of next unstarted order if there are any in queue
        if (workshopSim.unstartedOrders.size() > 0) {
            //Update statistics
            double durationOfPreviousQueueState = currentSimulationTime - workshopSim.lastUnstartedOrdersQueueChangeTime;
            workshopSim.averageCountOfUnstartedOrdersReplicationStatistic.addData(workshopSim.unstartedOrders.size(), durationOfPreviousQueueState);
            workshopSim.lastUnstartedOrdersQueueChangeTime = currentSimulationTime;

            Order nextUnstartedOrder = workshopSim.unstartedOrders.poll();
            Workplace nextFreeWorkplace;

            if (workshopSim.freeWorkplaces.size() > 0) {
                nextFreeWorkplace = workshopSim.freeWorkplaces.poll();
            } else {
                nextFreeWorkplace = new Workplace(workshopSim.nextAvailableWorkplaceNumber);
                workshopSim.allWorkplaces.add(nextFreeWorkplace);
                workshopSim.nextAvailableWorkplaceNumber++;
            }

            nextFreeWorkplace.setState(WorkplaceState.WAITING_FOR_WORKER);
            nextUnstartedOrder.setAssignedWorkplace(nextFreeWorkplace);
            nextUnstartedOrder.setCurrentWorker(oldWorkerA);
            nextUnstartedOrder.setState(OrderState.IN_PROGRESS);
            oldWorkerA.setState(WorkerState.MOVING_TO_WAREHOUSE);

            moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
            MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, nextUnstartedOrder, workshopSim);
            workshopSim.addEvent(moveEvent);
            
        // Free workerA if there are no unstarted orders
        } else {
            oldWorkerA.setBusy(false, currentSimulationTime);
            oldWorkerA.setState(WorkerState.FREE_AT_WORKPLACE);
            workshopSim.freeWorkersA.add(oldWorkerA);
        }


        // If there is a free worker from group C primarily assign him to one of the orders waiting for forging if there are any
        if (workshopSim.freeWorkersC.size() > 0 && workshopSim.ordersWaitingForForging.size() > 0) {
            
            
            
            Order nextForgingOrder = workshopSim.ordersWaitingForForging.poll();
            Worker assignedWorkerC = workshopSim.freeWorkersC.poll();
            nextForgingOrder.setCurrentWorker(assignedWorkerC);
            nextForgingOrder.setLastFinishedProductionStage(ProductionStage.ASSEMBLY); // netreba? 
            nextForgingOrder.setState(OrderState.IN_PROGRESS);
            assignedWorkerC.setBusy(true, currentSimulationTime);
            

            if (assignedWorkerC.getLastOccupiedWorkplace() == nextForgingOrder.getAssignedWorkplace().getId()) {
                    
                nextForgingOrder.getAssignedWorkplace().setState(WorkplaceState.IN_USE_FORGING);
                assignedWorkerC.setState(WorkerState.FORGING);
                finishTime = workshopSim.closetForgingTimeGenerator.nextDouble();
                FinishForgingEvent finishForgingEvent = new FinishForgingEvent(currentSimulationTime + finishTime, nextForgingOrder, workshopSim);
                workshopSim.addEvent(finishForgingEvent);

            } else if (assignedWorkerC.isAtWarehouse()) {

                nextForgingOrder.getAssignedWorkplace().setState(WorkplaceState.WAITING_FOR_WORKER);
                assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
                moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
                MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, nextForgingOrder, workshopSim);
                workshopSim.addEvent(moveEvent);

            } else {

                nextForgingOrder.getAssignedWorkplace().setState(WorkplaceState.WAITING_FOR_WORKER);
                assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
                moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
                MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, nextForgingOrder, workshopSim);
                workshopSim.addEvent(moveEvent);
                
            }

        // If there is still worker from C that is free, Start the next production stage for current order
        } else if (workshopSim.freeWorkersC.size() > 0) {

            Worker assignedWorkerC = workshopSim.freeWorkersC.poll();
            this.order.setCurrentWorker(assignedWorkerC);
            assignedWorkerC.setBusy(true, currentSimulationTime);

            
            if (assignedWorkerC.getLastOccupiedWorkplace() == this.order.getAssignedWorkplace().getId()) {
                
                switch(this.order.getOrderType()) {
                    case TABLE:
                        finishTime = workshopSim.tableVarnishingTimeGenerator.nextDouble();
                        break;
                    case CHAIR:
                        finishTime = workshopSim.chairVarnishingTimeGenerator.nextDouble();
                        break;
                    case CLOSET:
                        finishTime = workshopSim.closetVarnishingTimeGenerator.nextDouble();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid order type");
                }

                this.order.getAssignedWorkplace().setState(WorkplaceState.IN_USE_VARNISHING);
                assignedWorkerC.setState(WorkerState.VARNISHING);
                FinishVarnishingEvent finishVarnishingEvent = new FinishVarnishingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishVarnishingEvent);

            // Worker is at warehouse at the start of simulation
            } else if (assignedWorkerC.isAtWarehouse()) {

                this.order.getAssignedWorkplace().setState(WorkplaceState.WAITING_FOR_WORKER);
                assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
                moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
                MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, this.order, workshopSim);
                workshopSim.addEvent(moveEvent);

            // Worker is not at warehouse, he is at workplace but different than assigned to current order
            } else {

                this.order.getAssignedWorkplace().setState(WorkplaceState.WAITING_FOR_WORKER);
                assignedWorkerC.setState(WorkerState.MOVING_TO_WORKPLACE);
                moveTime = workshopSim.moveTimeBetweenWorkplacesGenerator.nextDouble();
                MoveBetweenWorkplacesEvent moveEvent = new MoveBetweenWorkplacesEvent(currentSimulationTime + moveTime, this.order, workshopSim);
                workshopSim.addEvent(moveEvent);

            }

        // There are no free workers from group C, put the order in the queue for next production stage
        } else {

            this.order.getAssignedWorkplace().setState(WorkplaceState.WAITING_FOR_WORKER);
            this.order.setState(OrderState.IN_QUEUE_FOR_VARNISHING);
            workshopSim.ordersWaitingForVarnishing.add(this.order);
        }


        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }
        
    }

}
