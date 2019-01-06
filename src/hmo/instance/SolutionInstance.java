package hmo.instance;

import hmo.common.RandomAccessSet;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;

import java.util.*;
import java.util.stream.Collectors;

public class SolutionInstance {

  private Problem problem;
  private Map<Vehicle, VehicleInstance> assignedVehicles;
  private Map<Track, TrackInstance> assignedTracks;
  private RandomAccessSet<Vehicle> vehiclePool;

  public SolutionInstance(Problem problem) {
    this.problem = problem;
    this.assignedTracks = new HashMap<>(this.problem.getTracks().size());
    this.assignedVehicles = new HashMap<>(this.problem.getVehicles().size());
    this.vehiclePool = new RandomAccessSet<>(problem.getVehicles());
  }

  public Problem getProblem() {
    return problem;
  }

  /** @return track instances, ordered by their respective ID low-high. */
  public Collection<TrackInstance> getTrackInstances() {
    // tracks must be sorted by their IDs low-high
    return assignedTracks.values().stream()
        .sorted(Comparator.comparingInt(ti -> ti.getTrack().getId()))
        .collect(Collectors.toList());
  }

  public Collection<VehicleInstance> getVehicleInstances() {
    return assignedVehicles.values();
  }

  public Collection<Vehicle> getVehiclePool() {
    return vehiclePool;
  }

  public Vehicle nextRandomVehicle(Random random) {
    return vehiclePool.pollRandom(random);
  }

  public Collection<Vehicle> getUnassignedVehicles() {
    Set<Vehicle> cars = new HashSet<>(problem.getVehicles());
    cars.removeAll(assignedVehicles.keySet());
    return cars;
  }

  // (7) departure of any vehicle that comes first must be before the departure of another vehicle
  // -> this is handled from within TrackInstance

  // TODO this does not take into account the fact that
  // (8) blocking tracks must come before the tracks that they block
  public boolean canAssign(Vehicle vehicle, Track track) {
    boolean trackConditions = track.getAllowedVehicleIds().contains(vehicle.getId())
        && track.getTrackLength() >= vehicle.getVehicleLength();
    boolean trackInstanceConditions = !assignedTracks.containsKey(track)
        || assignedTracks.get(track).canAdd(vehicle);
    return trackConditions && trackInstanceConditions;
  }

  public void assign(Vehicle vehicle, Track track) {
    VehicleInstance vehicleInstance = new VehicleInstance(vehicle, track);
    TrackInstance trackInstance = assignedTracks.getOrDefault(track, new TrackInstance(track));
    trackInstance.add(vehicleInstance);

    assignedVehicles.put(vehicle, vehicleInstance);
    assignedTracks.put(track, trackInstance);
    vehiclePool.remove(vehicle);
  }

  public int getNumOfUsedTracks() {
    int used = 0;
    for (TrackInstance track : assignedTracks.values()) {
      if (!track.getParkedVehicles().isEmpty()) {
        used++;
      }
    }
    return used;
  }
}
