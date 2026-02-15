package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Workplace;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderState;

public class OrderArrivalEvent extends GeneralEvent {

    public OrderArrivalEvent(double time, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();

        int typeOfOrder = workshopSim.orderTypeGenerator.nextInt();

        Order newOrder;
        OrderType orderType;

        switch(typeOfOrder) {
            case 1:
                orderType = OrderType.TABLE;
                break;
            case 2:
            orderType = OrderType.CHAIR;
                break;
            case 3:
            orderType = OrderType.CLOSET;
                break;
            default:
                throw new IllegalArgumentException("Invalid order type");
        }

    
        newOrder = new Order(workshopSim.nextAvailableOrderID, orderType, null, null, currentSimulationTime);
        
        if (workshopSim.isSimulationSlowdown()) {
            workshopSim.activeOrders.add(newOrder);
        }
       
        workshopSim.nextAvailableOrderID++;
        

        if (workshopSim.freeWorkersA.size() > 0) {

            Workplace nextFreeWorkplace;

            if (workshopSim.freeWorkplaces.size() > 0) {
                nextFreeWorkplace = workshopSim.freeWorkplaces.poll();
            } else {
                nextFreeWorkplace = new Workplace(workshopSim.nextAvailableWorkplaceNumber);
                workshopSim.allWorkplaces.add(nextFreeWorkplace);
                workshopSim.nextAvailableWorkplaceNumber++;
            }
            nextFreeWorkplace.setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
            newOrder.setAssignedWorkplace(nextFreeWorkplace);

            Worker assignedWorker = workshopSim.freeWorkersA.poll();
            assignedWorker.setBusy(true, currentSimulationTime);
            newOrder.setCurrentWorker(assignedWorker);
            newOrder.setState(OrderState.IN_PROGRESS);
            

            // Worker is at warehouse at start of simulation
            if (assignedWorker.isAtWarehouse()) {
                assignedWorker.setState(WorkerState.PREPARING_MATERIAL);
                double finishMaterialPrepTime = workshopSim.timeToPrepareMaterialGenerator.nextDouble();
                FinishMaterialPrepEvent finishMaterialPrepEvent = new FinishMaterialPrepEvent(currentSimulationTime + finishMaterialPrepTime, newOrder, workshopSim);
                workshopSim.addEvent(finishMaterialPrepEvent);

            // Worker is not at warehouse
            } else {
                assignedWorker.setState(WorkerState.MOVING_TO_WAREHOUSE);
                double moveTime = workshopSim.moveTimeBetweenWarehouseWorkplaceGenerator.nextDouble();
                MoveBetweenWarehouseWorkplaceEvent moveEvent = new MoveBetweenWarehouseWorkplaceEvent(currentSimulationTime + moveTime, newOrder, workshopSim);
                workshopSim.addEvent(moveEvent);
            }


        } else {

            newOrder.setState(OrderState.UNSTARTED);

            //Update statistics
            double durationOfPreviousQueueState = currentSimulationTime - workshopSim.lastUnstartedOrdersQueueChangeTime;
            workshopSim.averageCountOfUnstartedOrdersReplicationStatistic.addData(workshopSim.unstartedOrders.size(), durationOfPreviousQueueState);
            workshopSim.lastUnstartedOrdersQueueChangeTime = currentSimulationTime;

            workshopSim.unstartedOrders.add(newOrder);
            
        }

        // Schedule next order arrival event
        OrderArrivalEvent nextOrderArrivalEvent = new OrderArrivalEvent(currentSimulationTime + workshopSim.orderFlowGenerator.nextDouble(), this.simulation);
        workshopSim.addEvent(nextOrderArrivalEvent);


        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);
        
        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }

    }

}
