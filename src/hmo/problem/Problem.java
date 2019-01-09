package hmo.problem;

import hmo.instance.TrackInstance;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Problem {
  private List<Track> tracks;
  private List<Vehicle> vehicles;
  private Map<Integer, Collection<Integer>> trackBlocksTracks;
  private Map<Integer, Collection<Integer>> trackBlockedByTracks;
  private int totalTrackLength;
  private int totalVehicleLength;

  public Problem(List<Track> tracks, List<Vehicle> vehicles,
      Map<Integer, Collection<Integer>> trackBlocksTracks,
      Map<Integer, Collection<Integer>> trackBlockedByTracks) {
    this.tracks = tracks;
    this.vehicles = vehicles;
    this.trackBlocksTracks = trackBlocksTracks;
    this.trackBlockedByTracks = trackBlockedByTracks;
    setTotalTrackLength();
    setTotalVehicleLength();
  }

  public List<Track> getTracks() {
    return tracks;
  }

  public List<Vehicle> getVehicles() {
    return vehicles;
  }

  public Collection<Integer> getBlocks(int trackId) {
    return trackBlocksTracks.getOrDefault(trackId, new HashSet<>());
  }

  public Stream<Track> getBlocks(Track track) {
    Collection<Integer> blocks = trackBlocksTracks.getOrDefault(track.getId(), new HashSet<>());
    return blocks.stream().map(i -> tracks.get(i - 1));
  }

  public Collection<Integer> getBlockedBy(int trackId) {
    return trackBlockedByTracks.getOrDefault(trackId, new HashSet<>());
  }

  public Stream<Track> getBlockedBy(Track track) {
    // NOTE: assumes ID is index + 1
    Collection<Integer> blockedBy = getBlockedBy(track.getId());
    return blockedBy.stream().map(i -> tracks.get(i - 1));
  }

  public TrackInstance getBlockedTrack(TrackInstance first, TrackInstance second) {
    int fstId = first.getTrack().getId();
    int sndId = second.getTrack().getId();

    if (getBlocks(fstId).contains(sndId)) {
      return second;
    }
    if (getBlocks(sndId).contains(fstId)) {
      return first;
    }
    return null;
  }

  private void setTotalTrackLength() {
    int len = 0;
    for (Track track : tracks) {
      len += track.getTrackLength();
    }
    this.totalTrackLength = len;
  }

  private void setTotalVehicleLength() {
    int len = 0;
    for (Vehicle vehicle : vehicles) {
      len += vehicle.getVehicleLength();
    }
    this.totalVehicleLength = len;
  }

  public int getTotalTrackLength() {
    return totalTrackLength;
  }

  public int getTotalVehicleLength() {
    return totalVehicleLength;
  }
}
