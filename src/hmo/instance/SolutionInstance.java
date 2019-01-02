package hmo.instance;

import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SolutionInstance {

  private Problem problem;
  private Map<Vehicle, VehicleInstance> assignedVehicles;
  private Map<Track, TrackInstance> assignedTracks;

  public SolutionInstance(Problem problem) {
    this.problem = problem;
    this.assignedTracks = new HashMap<>();
    this.assignedVehicles = new HashMap<>();
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

  public boolean canAssign(Vehicle vehicle, Track track) {
    return this.assignedTracks.containsKey(track) &&
        this.assignedTracks.get(track).canAdd(vehicle);
  }

  public void assign(Vehicle vehicle, Track track) {
    VehicleInstance vehicleInstance = new VehicleInstance(vehicle, track);
    TrackInstance trackInstance = assignedTracks.getOrDefault(track, new TrackInstance(track));
    trackInstance.add(vehicleInstance);

    assignedVehicles.put(vehicle, vehicleInstance);
    assignedTracks.put(track, trackInstance);
  }
}
