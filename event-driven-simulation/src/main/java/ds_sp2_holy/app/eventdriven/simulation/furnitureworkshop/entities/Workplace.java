package ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities;

public class Workplace implements Comparable<Workplace> {

    private int id;
    private WorkplaceState state;

    public Workplace (int id) {
        this.id = id;
        this.state = WorkplaceState.FREE;
    }
    

    public enum WorkplaceState {
        FREE("Free"),
        WAITING_FOR_WORKER("Waiting for worker"),
        IN_USE_CUTTING("In use cutting"),
        IN_USE_VARNISHING("In use varnishing"),
        IN_USE_ASSEMBLING("In use assembling"),
        IN_USE_FORGING("In use forging"),
        IN_USE_EXPEDITION("In use expedition");


        private final String state;

        WorkplaceState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }
    }

    public int getId() {
        return id;
    }

    public WorkplaceState getState() {
        return state;
    }

    public void setState(WorkplaceState state) {
        this.state = state;
    }

    @Override
    public int compareTo(Workplace other) {
        return Integer.compare(this.id, other.id);
    }



}
