package hmo.problem;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Track {

  private int id;
  private int trackLength;
  private Set<Integer> allowedVehicleIds;

  public Track(int id, int trackLength, Set<Integer> allowedVehicleIds) {
    this.id = id;
    this.trackLength = trackLength;
    this.allowedVehicleIds = Collections.unmodifiableSet(allowedVehicleIds);
  }

  public int getId() {
    return id;
  }

  public Set<Integer> getAllowedVehicleIds() {
    return allowedVehicleIds;
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
    return id == track.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Track{" +
        "id=" + id +
        ", trackLength=" + trackLength +
        ", allowedVehicleIds=" + allowedVehicleIds.stream()
        .map(Object::toString)
        .collect(Collectors.joining(",")) +
        '}';
  }
}
