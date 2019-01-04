package hmo.instance;

import hmo.common.RandomAccessSet;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

  public Collection<TrackInstance> getTrackInstances() {
    return assignedTracks.values();
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
}
