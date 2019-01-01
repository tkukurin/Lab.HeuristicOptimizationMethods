package hmo.problem;

import java.util.ArrayList;
import java.util.Objects;

public class Track {

  private int ord;
  private int trackLength;
  private ArrayList<Integer> restrictions;

  public Track(int ord, ArrayList<Integer> restrictions, int trackLength) {
    this.ord = ord;
    this.restrictions = restrictions;
    this.trackLength = trackLength;
  }

  public int getOrd() {
    return ord;
  }

  public ArrayList<Integer> getRestrictions() {
    return restrictions;
  }

  public int getTrackLength() {
    return trackLength;
  }

  // ordinal number defines a single track.
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Track track = (Track) o;
    return ord == track.ord;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ord);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Num = " + ord + "len = " + trackLength + ", restrictions : ");
    for (int i = 0; i < restrictions.size() - 1; i++) {
      sb.append(restrictions.get(i) + ", ");
    }
    sb.append(restrictions.get(restrictions.size() - 1));
    return sb.toString();
  }
}
