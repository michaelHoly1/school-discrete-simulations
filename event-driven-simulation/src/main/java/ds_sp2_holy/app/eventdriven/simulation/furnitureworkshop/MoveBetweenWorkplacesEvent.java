package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop;

import ds_sp2_holy.app.eventdriven.simulation.*;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate.RefreshType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order.OrderType;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.*;

public class MoveBetweenWorkplacesEvent extends GeneralEvent {

    private Order order;

    public MoveBetweenWorkplacesEvent(double time, Order order, GeneralEventDrivenSimulation simulation) {
        super(time, simulation);
        this.order = order;
    }

    @Override
    public void execute() {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) this.simulation;
        double currentSimulationTime = workshopSim.getCurrentSimulationTime();

        Worker assignedWorker = this.order.getCurrentWorker();
        assignedWorker.setAtWorkplace(true);
        assignedWorker.setAtWarehouse(false);
        assignedWorker.setLastOccupiedWorkplace(this.order.getAssignedWorkplace().getId());

        double finishTime;

        switch (this.order.getLastFinishedProductionStage()) {

            case CUTTING:
                
                

                switch (this.order.getOrderType()) {

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
                assignedWorker.setState(Worker.WorkerState.VARNISHING);
                FinishVarnishingEvent finishVarnishingEvent = new FinishVarnishingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishVarnishingEvent);
                break;

            case VARNISHING:

                   

                switch (this.order.getOrderType()) {

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
                assignedWorker.setState(Worker.WorkerState.ASSEMBLING); 
                FinishAssemblyEvent finishAssemblyEvent = new FinishAssemblyEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                workshopSim.addEvent(finishAssemblyEvent);
                break;

            case ASSEMBLY:

                
                if (this.order.getOrderType() == OrderType.CLOSET) {
                    assignedWorker.setState(Worker.WorkerState.FORGING);
                    this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_FORGING);
                    finishTime = workshopSim.closetForgingTimeGenerator.nextDouble();
                    FinishForgingEvent finishForgingEvent = new FinishForgingEvent(currentSimulationTime + finishTime, this.order, workshopSim);
                    workshopSim.addEvent(finishForgingEvent);
                } else {
                    assignedWorker.setState(Worker.WorkerState.EXPEDING_ORDER);
                    this.order.getAssignedWorkplace().setState(Workplace.WorkplaceState.IN_USE_EXPEDITION);
                    finishTime = currentSimulationTime;
                    ReadyForExpeditionEvent readyForExpeditionEvent = new ReadyForExpeditionEvent(currentSimulationTime, this.order, workshopSim);
                    workshopSim.addEvent(readyForExpeditionEvent);

                }

                
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
