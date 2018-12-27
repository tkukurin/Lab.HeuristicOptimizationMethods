import java.util.ArrayList;
import java.util.List;

public class Track {

    private int ord;
    private ArrayList<Integer> restrictions;
    private int trackLength;
    private int availableSpace;
    private List<Vehicle> parkedVehicles;

    public Track(int ord, ArrayList<Integer> restrictions, int trackLength) {
        this.ord = ord;
        this.restrictions = restrictions;
        this.trackLength = trackLength;
        this.availableSpace = trackLength;
        this.parkedVehicles = new ArrayList<>();
    }

    public int getOrd() {
        return ord;
    }

    public ArrayList<Integer> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(ArrayList<Integer> restrictions) {
        this.restrictions = restrictions;
    }

    public int getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(int trackLength) {
        this.trackLength = trackLength;
    }

    public boolean addVehicle(Vehicle vehicle) {
        if (availableSpace > vehicle.getVehicleLength() && restrictions.get(vehicle.getOrd()) == 1) {
            parkedVehicles.add(vehicle);
            this.availableSpace -= vehicle.getVehicleLength();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Len = " + trackLength + ", restrictions : ");
        for (int i = 0; i < restrictions.size() - 1; i++) {
            sb.append(restrictions.get(i) + ", ");
        }
        sb.append(restrictions.get(restrictions.size() - 1));
        return sb.toString();
    }
}
