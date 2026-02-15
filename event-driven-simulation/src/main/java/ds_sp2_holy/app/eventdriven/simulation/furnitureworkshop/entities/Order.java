package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities;


public class Order {

    private OrderState orderState;
    private OrderType orderType;
    private int orderID;
    private Worker currentWorker;
    private Workplace assignedWorkplace;
    private ProductionStage lastFinishedProductionStage;
    private double simulationTimeAtStart;


    public Order(int orderID, OrderType orderType, Worker currentWorker, Workplace assignedWorkplace, double simulationTimeAtStart) {
        this.orderID = orderID;
        this.orderType = orderType;
        this.currentWorker = currentWorker;
        this.assignedWorkplace = assignedWorkplace;
        this.simulationTimeAtStart = simulationTimeAtStart;
        this.lastFinishedProductionStage = ProductionStage.UNSTARTED;
        this.orderState = OrderState.UNSTARTED;
    }
    

    public enum OrderState {

        
        UNSTARTED("Unstarted"),
        IN_PROGRESS("In progress"),
        IN_QUEUE_FOR_VARNISHING("In queue for varnishing"),
        IN_QUEUE_FOR_ASSEMBLY("In queue for assembly"),
        IN_QUEUE_FOR_FORGING("In queue for forging"),
        IN_QUEUE_FOR_EXPEDITION("In queue for expedition"),
        FINISHED("Finished");

        private final String state;

        OrderState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }

    }
    
    
    
    public enum OrderType {
        TABLE("Table order"),
        CHAIR("Chair order"),
        CLOSET("Closet order");

        private final String orderType;

        OrderType(String orderType) {
            this.orderType = orderType;
        }

        @Override
        public String toString() {
            return orderType;
        }
    }

    public enum ProductionStage {
        UNSTARTED("Unstarted"),
        MATERIAL_PREP("Material preparation"),
        CUTTING("Cutting"),
        VARNISHING("Varnishing"),
        ASSEMBLY("Assembly"),
        FORGING("Forging"),
        EXPEDITION("Expedition"),
        FINISHED("Finished");

        private final String stage;

        ProductionStage(String stage) {
            this.stage = stage;
        }

        @Override
        public String toString() {
            return stage;
        }

    }

    public int getOrderID() {
        return orderID;
    }

    public OrderState getState() {
        return orderState;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public String getOrderInfo() {
        return orderID + ". " + orderType.toString();
    }

    public Worker getCurrentWorker() {
        return currentWorker;
    }

    public Workplace getAssignedWorkplace() {
        return assignedWorkplace;
    }

    public double getSimulationTimeAtStart() {
        return simulationTimeAtStart;
    }

    public ProductionStage getLastFinishedProductionStage() {
        return lastFinishedProductionStage;
    }

    public void setCurrentWorker(Worker currentWorker) {
        this.currentWorker = currentWorker;
    }

    public void setAssignedWorkplace(Workplace assignedWorkplace) {
        this.assignedWorkplace = assignedWorkplace;
    }

    public void setLastFinishedProductionStage(ProductionStage lastFinishedProductionStage) {
        this.lastFinishedProductionStage = lastFinishedProductionStage;
    }

    public void setState(OrderState orderState) {
        this.orderState = orderState;
    }

    


}
