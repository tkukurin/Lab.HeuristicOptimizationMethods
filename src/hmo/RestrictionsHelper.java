package hmo;

import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RestrictionsHelper {

  private final Map<String, Supplier<Boolean>> restrictionNameToCheck = new HashMap<>();
  private final SolutionInstance solutionInstance;

  public RestrictionsHelper(SolutionInstance solutionInstance) {
    this.solutionInstance = solutionInstance;
    restrictionNameToCheck.put("Vehicle should appear only once.", this::vehicleAppearsOnlyOnce);
    restrictionNameToCheck.put("Single series in track", this::singleSeriesInTracksTest);
    restrictionNameToCheck.put(
        "Vehicle in correct track", this::vehiclesAllowedInAssignedTracksTest);
    restrictionNameToCheck.put("Track length", this::tracksNotOverloadedTest);
    restrictionNameToCheck.put(
        "Blocked vehicles depart first", this::vehiclesInBlockedTracksDepartureTimesTest);
    restrictionNameToCheck.put(
        "Vehicle only once on a track", this::vehicleOnlyOnceInATrackTest);
    restrictionNameToCheck.put(
        "Order of departure on a single track",
        this::orderOfDepartureOfVehiclesInTheSameTrackTest);
  }

  public Map<String, Supplier<Boolean>> getRestrictionChecks() {
    return restrictionNameToCheck;
  }

  public boolean vehicleAppearsOnlyOnce() {
    // test not needed, vehicleInstance can have just one Track
    // -> we are actually testing for duplicate vehicles, so this can happen.

    Set<Vehicle> foundVehicles = new HashSet<>();
    Iterator<Vehicle> allVehiclesInTracks = solutionInstance.getTrackInstances().stream()
        .flatMap(trackInstance -> trackInstance.getParkedVehicles().stream())
        .map(VehicleInstance::getVehicle)
        .iterator();

    while (allVehiclesInTracks.hasNext()) {
      Vehicle vehicle = allVehiclesInTracks.next();
      if (foundVehicles.contains(vehicle)) {
        return false;
      }

      foundVehicles.add(vehicle);
    }

    return true;
  }

  public boolean singleSeriesInTracksTest() {
    for (TrackInstance track : solutionInstance.getTrackInstances()) {
      if (!track.getParkedVehicles().isEmpty()) {
        int series = -1;
        boolean first = true;
        for (VehicleInstance vehicle : track.getParkedVehicles()) {
          if (first) {
            first = false;
            series = vehicle.getVehicle().getSeries();
          } else {
            if (vehicle.getVehicle().getSeries() != series) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  public boolean vehiclesAllowedInAssignedTracksTest() {
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      Collection<Integer> allowedVehicles = track.getTrack().getAllowedVehicleIds();
      for (VehicleInstance vehicle : track.getParkedVehicles()) {
        int vehicleOrder = vehicle.getVehicle().getId();
        if (!allowedVehicles.contains(vehicleOrder)) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean tracksNotOverloadedTest() {
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      double trackLenLeft = (double) track.getTrack().getTrackLength();
      for (VehicleInstance vehicle : track.getParkedVehicles()) {
        trackLenLeft -= ((double) vehicle.getVehicle().getVehicleLength() + 0.5);
      }
      // one gap between vehicles too many has to be removed
      trackLenLeft += 0.5;
      if (trackLenLeft < 0) {
        return false;
      }
    }
    return true;
  }

  public boolean vehicleOnlyOnceInATrackTest() {
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      List<VehicleInstance> vehiclesInATrack = new ArrayList<>();
      for (VehicleInstance vehicle : track.getParkedVehicles()) {
        if (vehiclesInATrack.contains(vehicle)) {
          return false;
        }
        vehiclesInATrack.add(vehicle);
      }
    }
    return true;
  }

  public boolean orderOfDepartureOfVehiclesInTheSameTrackTest() {
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      int departure = -1;
      for (VehicleInstance vehicle : track.getParkedVehicles()) {
        int nextDeparture = vehicle.getVehicle().getDeparture();
        if (nextDeparture <= departure) {
          return false;
        }
        departure = nextDeparture;
      }
    }
    return true;
  }

  public boolean vehiclesInBlockedTracksDepartureTimesTest() {
    Map<Integer, TrackInstance> idToTrackInstance = solutionInstance.getTrackInstances()
        .stream().collect(Collectors.toMap(ti -> ti.getTrack().getId(), ti -> ti));

    for (Entry<Integer, TrackInstance> idAndTrackInstance : idToTrackInstance.entrySet()) {
      int id = idAndTrackInstance.getKey();
      TrackInstance trackInstance = idAndTrackInstance.getValue();

      if (trackInstance.getParkedVehicles().isEmpty()) {
        continue;
      }

      int firstDeparture = trackInstance.getParkedVehicles().get(0).getVehicle().getDeparture();
      for (Integer blockingId : solutionInstance.getProblem().getBlockedBy(id)) {
        TrackInstance blockingInstance = idToTrackInstance.get(blockingId);
        int len = blockingInstance.getParkedVehicles().size();
        if (len == 0) {
          continue;
        }

        int lastDeparture = blockingInstance
            .getParkedVehicles()
            .get(len - 1)
            .getVehicle()
            .getDeparture();
        if (lastDeparture >= firstDeparture) {
          return false;
        }
      }
    }

    return true;
  }

  public Map<Track, Collection<Track>> collectBlockers() {
    Map<Integer, TrackInstance> idToTrackInstance = solutionInstance.getTrackInstances()
        .stream().collect(Collectors.toMap(ti -> ti.getTrack().getId(), ti -> ti));
    Map<Track, Collection<Track>> result = new HashMap<>();

    for (Entry<Integer, TrackInstance> idAndTrackInstance : idToTrackInstance.entrySet()) {
      int id = idAndTrackInstance.getKey();
      TrackInstance trackInstance = idAndTrackInstance.getValue();
      Collection<Track> blockedBy = new HashSet<>();
      result.put(trackInstance.getTrack(), blockedBy);

      if (trackInstance.getParkedVehicles().isEmpty()) {
        continue;
      }

      int firstDeparture = trackInstance.getParkedVehicles().get(0).getVehicle().getDeparture();
      for (Integer blockingId : solutionInstance.getProblem().getBlockedBy(id)) {
        TrackInstance blockingInstance = idToTrackInstance.get(blockingId);
        int len = blockingInstance.getParkedVehicles().size();
        if (len == 0) {
          continue;
        }

        List<VehicleInstance> parkedVehicles = blockingInstance
            .getParkedVehicles();
        int lastDeparture = parkedVehicles
            .get(parkedVehicles.size() - 1)
            .getVehicle()
            .getDeparture();
        if (lastDeparture >= firstDeparture) {
          blockedBy.add(blockingInstance.getTrack());
        }
      }
    }

    return result;
  }

}
