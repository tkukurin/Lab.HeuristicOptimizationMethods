package hmo.instance;

import hmo.common.RandomAccessSet;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.util.*;
import java.util.stream.Collectors;

public class SolutionInstance {

  private Problem problem;
  private Map<Vehicle, VehicleInstance> assignedVehicles;
  private Map<Track, TrackInstance> trackToIstance;
  private RandomAccessSet<Vehicle> vehiclePool;

  public SolutionInstance(Problem problem) {
    this.problem = problem;
    this.trackToIstance = problem.getTracks().stream().map(TrackInstance::new)
        .collect(Collectors.toMap(TrackInstance::getTrack, ti -> ti));
    this.assignedVehicles = new HashMap<>(this.problem.getVehicles().size());
    this.vehiclePool = new RandomAccessSet<>(problem.getVehicles());
  }

  public Problem getProblem() {
    return problem;
  }

  /** @return track instances, ordered by their respective ID low-high. */
  public List<TrackInstance> getTrackInstancesInorder() {
    return trackToIstance.values().stream()
        .sorted(Comparator.comparingInt(ti -> ti.getTrack().getId()))
        .collect(Collectors.toList());
  }

  public Collection<VehicleInstance> getVehicleInstances() {
    return assignedVehicles.values();
  }

  public Collection<Vehicle> getVehiclePool() {
    return vehiclePool;
  }

  public Vehicle pollRandomVehicle(Random random) {
    return vehiclePool.pollRandom(random);
  }

  public void returnToPool(Vehicle ... vehicles) {
    vehiclePool.addAll(Arrays.asList(vehicles));
  }

  public TrackInstance getRandomTrack(Random random) {
    int nTracks = trackToIstance.size();
    return trackToIstance.get(problem.getTracks().get(random.nextInt(nTracks)));
  }

  public TrackInstance getRandomTrack(Random random, Integer seriesType) {
    if (seriesType == null) {
      return getRandomTrack(random);
    }

    List<TrackInstance> validTracks = trackToIstance.values()
        .stream()
        .filter(ti ->
            ti.getAllowedVehicleSeries() == null
                || ti.getAllowedVehicleSeries().equals(seriesType))
        .collect(Collectors.toList());
    if (validTracks.isEmpty()) {
      return null;
    }

    int nTracks = validTracks.size();
    return validTracks.get(random.nextInt(nTracks));
  }

  /** @return all vehicles which haven't been assigned to any tracks so far */
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
    boolean trackInstanceConditions = trackToIstance.get(track).canAdd(vehicle);

    // this track is blocked by some tracks.
    // the cars on those other tracks *must* depart *before* this car
    boolean blockingConditions = problem.getBlockedBy(track.getId())
        .stream()
        .map(id -> problem.getTracks().get(id))
        .map(t -> trackToIstance.get(t))
        .map(TrackInstance::getParkedVehicles)
        .anyMatch(pvs -> pvs.stream()
            .map(VehicleInstance::getVehicle)
            .map(Vehicle::getDeparture)
            .allMatch(dt -> dt <= vehicle.getDeparture()));

    return trackConditions && trackInstanceConditions;
        //&& blockingConditions;
  }

  public void assign(Vehicle vehicle, Track track) {
    VehicleInstance vehicleInstance = new VehicleInstance(vehicle, track);
    TrackInstance trackInstance = trackToIstance.get(track);
    trackInstance.add(vehicleInstance);

    assignedVehicles.put(vehicle, vehicleInstance);
    vehiclePool.remove(vehicle);
  }

  public int getNumOfUsedTracks() {
    int used = 0;
    for (TrackInstance track : trackToIstance.values()) {
      if (!track.getParkedVehicles().isEmpty()) {
        used++;
      }
    }
    return used;
  }
}
