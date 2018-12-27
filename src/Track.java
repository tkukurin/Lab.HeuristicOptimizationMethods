import java.util.ArrayList;

public class Track {

    private ArrayList<Integer> restrictions;
    private int trackLength;

    public Track(ArrayList<Integer> restrictions, int trackLength) {
        this.restrictions = restrictions;
        this.trackLength = trackLength;
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
