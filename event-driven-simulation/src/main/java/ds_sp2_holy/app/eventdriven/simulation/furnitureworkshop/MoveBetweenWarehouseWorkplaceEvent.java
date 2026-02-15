package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker.WorkerState;

public class MoveBetweenWarehouseWorkplaceEvent extends GeneralEvent {

    private Order order;
    
    public MoveBetweenWarehouseWorkplaceEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();
        Worker assignedWorker = this.order.getCurrentWorker();

        assignedWorker.setAtWarehouse(false);
        assignedWorker.setAtWorkplace(true);
        assignedWorker.setLastOccupiedWorkplace(this.order.getAssignedWorkplace().getId());
        
        double finishTime;

        switch(this.order.getLastFinishedProductionStage()) {
            case UNSTARTED:

                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.WAITING_FOR_WORKER);
                assignedWorker.setAtWarehouse(true);
                assignedWorker.setAtWorkplace(false);
                assignedWorker.setState(WorkerState.PREPARING_MATERIAL);
                finishTime = workshopSim.timeToPrepareMaterialGenerator.nextDouble();
                FinishMaterialPrepEvent finishMaterialPrepEvent = new FinishMaterialPrepEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishMaterialPrepEvent);
                break;

            case MATERIAL_PREP:

                
            
                switch (this.order.getOrderType()) {
                    case TABLE:
                        finishTime = workshopSim.tableCuttingTimeGenerator.nextDouble();
                        break;
                    case CHAIR:
                        finishTime = workshopSim.chairCuttingTimeGenerator.nextDouble();
                        break;
                    case CLOSET:
                        finishTime = workshopSim.closetCuttingTimeGenerator.nextDouble();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid order type");
                }

                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_CUTTING);
                assignedWorker.setState(WorkerState.CUTTING);
                FinishCuttingEvent finishCuttingEvent = new FinishCuttingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishCuttingEvent);
                break;

            case CUTTING:
                
                

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

                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_VARNISHING);
                assignedWorker.setState(WorkerState.VARNISHING);
                FinishVarnishingEvent finishVarnishingEvent = new FinishVarnishingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishVarnishingEvent);
                break;

            case VARNISHING:

                    

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
                assignedWorker.setState(WorkerState.ASSEMBLING);
                FinishAssemblyEvent finishAssemblyEvent = new FinishAssemblyEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishAssemblyEvent);
                break;

            case ASSEMBLY:
            
                this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_FORGING);
                assignedWorker.setState(WorkerState.FORGING);
                finishTime = workshopSim.closetForgingTimeGenerator.nextDouble();
                FinishForgingEvent finishForgingEvent = new FinishForgingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishForgingEvent);
                    
                
                break;
            default:
                throw new IllegalArgumentException("Invalid production stage");

        }


        // Update GUI
        workshopSim.refreshGUI(RefreshType.REFRESH_ENTITIES);

        if (workshopSim.isSimulationSlowdown() && currentSimulationTime > workshopSim.nextUpdateTimeForAnimation) {
            workshopSim.nextUpdateTimeForAnimation += workshopSim.animationUpdateInterval;
            workshopSim.refreshGUI(RefreshType.REFRESH_ANIMATION_STATISTICS);
        }
    }
}
