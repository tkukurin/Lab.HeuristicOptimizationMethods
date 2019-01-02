package hmo.problem;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class Track {

  private int id;
  private int trackLength;
  private Collection<Integer> allowedVehicleTypes;
  private Collection<Track> blockedByTrack;
  private Collection<Track> blocksTrack;

  public Track(int id, int trackLength, Collection<Integer> allowedVehicleTypes) {
    this.id = id;
    this.trackLength = trackLength;
    this.allowedVehicleTypes = allowedVehicleTypes;
  }

  // TODO this maybe isn't necessary
  public Track(int id, int trackLength, Collection<Integer> allowedTypes,
      Collection<Track> blockedByTrack, Collection<Track> blocksTrack) {
    this.id = id;
    this.allowedVehicleTypes = allowedTypes;
    this.trackLength = trackLength;
    this.blockedByTrack = blockedByTrack;
    this.blocksTrack = blocksTrack;
  }

  public int getId() {
    return id;
  }

  public Collection<Integer> getAllowedVehicleTypes() {
    return allowedVehicleTypes;
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
    return "Num = " + id + "len = " + trackLength + ", "
        + "allowedVehicleTypes : "
        + this.allowedVehicleTypes.stream()
        .map(Object::toString)
        .collect(Collectors.joining(","));
  }
}
