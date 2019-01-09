package hmo.instance;

import hmo.common.RandomAccessSet;
import hmo.common.TrackUtils;
import hmo.common.Utils;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolutionInstance {

  private Problem problem;
  private Map<Vehicle, VehicleInstance> assignedVehicles;
  private Map<Track, TrackInstance> trackToInstance;
  private RandomAccessSet<Vehicle> vehiclePool;

  public SolutionInstance(SolutionInstance other) {
    this(other.getProblem(), other.trackToInstance.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> new ArrayList<>(e.getValue().getParkedVehicles()))));
  }

  public SolutionInstance(Problem problem) {
    this(problem, Collections.emptyMap());
//    this.problem = problem;
//    this.trackToInstance = problem.getTracks().stream().map(TrackInstance::new)
//        .collect(Collectors.toMap(TrackInstance::getTrack, ti -> ti));
//    this.assignedVehicles = new HashMap<>(problem.getVehicles().size());
//    this.vehiclePool = new RandomAccessSet<>(problem.getVehicles());
  }

  public SolutionInstance(Problem problem, Map<Track, List<VehicleInstance>> tracks) {
    this.problem = problem;
    this.trackToInstance = problem.getTracks().stream()
        .map(TrackInstance::new)
        .peek(ti -> ti.setParkedVehicles(tracks.getOrDefault(ti.getTrack(), new ArrayList<>())))
        .collect(Collectors.toMap(TrackInstance::getTrack, ti -> ti));
    this.assignedVehicles = tracks.values().stream().flatMap(Collection::stream)
        .collect(Collectors.toMap(VehicleInstance::getVehicle, vi -> vi));
    this.vehiclePool = new RandomAccessSet<>(Utils.difference(
        problem.getVehicles(),
        tracks.values().stream()
            .flatMap(Collection::stream)
            .map(VehicleInstance::getVehicle)
            .collect(Collectors.toList())));
  }

  public Problem getProblem() {
    return problem;
  }

  public Collection<TrackInstance> getTrackInstances() {
    return trackToInstance.values();
  }

  /** @return track instances, ordered by their respective ID low-high. */
  public List<TrackInstance> getTrackInstancesInorder() {
    return trackToInstance.values().stream()
        .sorted(Comparator.comparingInt(ti -> ti.getTrack().getId()))
        .collect(Collectors.toList());
  }

  public Vehicle pollUsedVehicle(Track track, Random random) {
    VehicleInstance vehicleInstance = trackToInstance.get(track).popRandom(random);
    if (vehicleInstance == null) {
      return null;
    }
    return markUnassigned(vehicleInstance.getVehicle());
  }

  public Collection<Vehicle> pollUsedVehicles(Track track, Predicate<Vehicle> pollCondition) {
    TrackInstance trackInstance = trackToInstance.get(track);

    List<Vehicle> vehiclesToReturn = new ArrayList<>();
    List<VehicleInstance> vehiclesRemainingInTrack = new ArrayList<>();

    for (VehicleInstance parked : trackInstance.getParkedVehicles()) {
      Vehicle vehicle = parked.getVehicle();

      if (pollCondition.test(vehicle)) {
        vehiclesToReturn.add(markUnassigned(vehicle));
      } else {
        vehiclesRemainingInTrack.add(parked);
      }
    }

    trackInstance.setParkedVehicles(vehiclesRemainingInTrack);
    return vehiclesToReturn;
  }

  public Collection<VehicleInstance> getVehicleInstances() {
    return assignedVehicles.values();
  }

  public Collection<Vehicle> getVehiclePool() {
    return vehiclePool;
  }

  public Vehicle pollUnusedVehicle(Random random) {
    return vehiclePool.pollRandom(random);
  }

  public List<TrackInstance> getAllowedTracks(Vehicle vehicle) {
    // move this to a map if too slow.
    return trackToInstance.values().stream()
        .filter(ti -> ti.canAdd(vehicle))
        .collect(Collectors.toList());
  }

  public TrackInstance getRandomTrack(Random random) {
    int nTracks = trackToInstance.size();
    return trackToInstance.get(problem.getTracks().get(random.nextInt(nTracks)));
  }

  public TrackInstance getRandomTrack(Random random, Integer seriesType) {
    if (seriesType == null) {
      return getRandomTrack(random);
    }

    // move this to a map if too slow.
    List<TrackInstance> validTracks = trackToInstance.values()
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

  public RandomAccessSet<Vehicle> resetVehiclePool() {
    Collection<Vehicle> unassigned = getUnassignedVehicles();
    vehiclePool = new RandomAccessSet<>(unassigned);
    return vehiclePool;
  }

  /** @return all vehicles which haven't been assigned to any tracks so far */
  public Collection<Vehicle> getUnassignedVehicles() {
    return Utils.difference(problem.getVehicles(), assignedVehicles.keySet());
  }

  public Collection<Vehicle> getAssignedVehicles() {
    return assignedVehicles.keySet();
  }

  // handled from within TrackInstance: (7) departure of any vehicle that comes first must be
  // before the departure of another vehicle
  // not taken into account: (8) blocking tracks must come before the tracks that they block

  public boolean canAssign(Vehicle vehicle, Track track) {
    boolean trackConditions = track.getAllowedVehicleIds().contains(vehicle.getId())
        && track.getTrackLength() >= vehicle.getVehicleLength();
    boolean trackInstanceConditions = trackToInstance.get(track).canAdd(vehicle);
    return trackConditions && trackInstanceConditions;
  }

  public Stream<TrackInstance> getBlockers(Vehicle vehicle, Track track) {
    return problem.getBlockedBy(track.getId())
        .stream()
        .map(id -> problem.getTracks().get(id))
        .map(t -> trackToInstance.get(t))
        .filter(ti -> ti.getParkedVehicles().stream()
            .map(VehicleInstance::getVehicle)
            .map(Vehicle::getDeparture)
            // if any vehicle departs after the current vehicle and
            // that vehicle's track is blocking the current track
            .anyMatch(dt -> dt > vehicle.getDeparture()));
  }

  public void assign(Vehicle vehicle, Track track) {
    VehicleInstance vehicleInstance = new VehicleInstance(vehicle, track);
    TrackInstance trackInstance = trackToInstance.get(track);
    trackInstance.add(vehicleInstance);

    markAssigned(vehicle, vehicleInstance);
  }

  public void swapParkedVehicles(Track first, Track second) {
    TrackInstance firstInstance = trackToInstance.get(first);
    TrackInstance secondInstance = trackToInstance.get(second);

    List<VehicleInstance> firstVehicles = firstInstance.getParkedVehicles();
    List<VehicleInstance> secondVehicles = secondInstance.getParkedVehicles();

    if (TrackUtils.canSetVehicles(firstVehicles, secondInstance.getTrack())
      && TrackUtils.canSetVehicles(secondVehicles, firstInstance.getTrack())) {

      firstInstance.setParkedVehicles(secondVehicles.stream()
          .map(vi -> new VehicleInstance(vi.getVehicle(), first))
          .collect(Collectors.toList()));
      secondInstance.setParkedVehicles(firstVehicles.stream()
          .map(vi -> new VehicleInstance(vi.getVehicle(), second))
          .collect(Collectors.toList()));
    }
  }

  public Stream<Track> tracksForVehicle(Vehicle vehicle) {
    return trackToInstance.entrySet().stream()
        .filter(t -> TrackUtils.validId(vehicle, t.getKey()))
        .filter(t -> t.getValue().canAdd(vehicle))
        .map(Entry::getKey);
  }

  public int nUsedTracks() {
    int used = 0;
    for (TrackInstance track : trackToInstance.values()) {
      if (!track.getParkedVehicles().isEmpty()) {
        used++;
      }
    }
    return used;
  }

//  public void setParkedVehicles(Track track, List<VehicleInstance> vehicles) {
//    TrackInstance trackInstance = new TrackInstance(track);
//    trackInstance.setParkedVehicles(vehicles);
//    trackToInstance.put(track, trackInstance);
//    assignedVehicles.putAll(vehicles.stream().collect(
//        Collectors.toMap(VehicleInstance::getVehicle, v -> v)));
//  }

  private void markAssigned(Vehicle vehicle, VehicleInstance vehicleInstance) {
    assignedVehicles.put(vehicle, vehicleInstance);
    vehiclePool.remove(vehicle);
  }

  private Vehicle markUnassigned(Vehicle vehicle) {
    assignedVehicles.remove(vehicle);
    vehiclePool.add(vehicle);

    return vehicle;
  }

  public TrackInstance removeParkedVehiclesFromRandomTrack(Random random) {
    Track track = Utils.randomElement(problem.getTracks(), random);
    TrackInstance instance = trackToInstance.get(track);
    List<VehicleInstance> vehicles = instance.getParkedVehicles();
    instance.setParkedVehicles(new ArrayList<>());

    vehicles.stream().map(VehicleInstance::getVehicle)
        .forEach(this::markUnassigned);
    resetVehiclePool();
    return instance;
  }

  @Override
  public String toString() {
    return getTrackInstancesInorder().stream()
      .map(TrackInstance::toString)
      .collect(Collectors.joining("\n"));
  }
}
