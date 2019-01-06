package hmo.problem;

import hmo.instance.TrackInstance;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Problem {
  private List<Track> tracks;
  private List<Vehicle> vehicles;
  private Map<Integer, Collection<Integer>> trackBlocksTracks;
  private Map<Integer, Collection<Integer>> trackBlockedByTracks;

  public Problem(List<Track> tracks, List<Vehicle> vehicles,
      Map<Integer, Collection<Integer>> trackBlocksTracks,
      Map<Integer, Collection<Integer>> trackBlockedByTracks) {
    this.tracks = tracks;
    this.vehicles = vehicles;
    this.trackBlocksTracks = trackBlocksTracks;
    this.trackBlockedByTracks = trackBlockedByTracks;
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

  public Collection<Integer> getBlockedBy(int trackId) {
    return trackBlockedByTracks.getOrDefault(trackId, new HashSet<>());
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
}
