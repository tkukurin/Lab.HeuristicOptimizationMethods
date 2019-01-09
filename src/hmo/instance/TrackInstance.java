package hmo.instance;

import hmo.common.TrackUtils;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TrackInstance {

  private Track track;
  private double availableSpace;
  private Integer allowedVehicleSeries;
  private List<VehicleInstance> parkedVehicles;

  public TrackInstance(Track track) {
    this(track, Collections.emptyList());
  }

  public TrackInstance(Track track, List<VehicleInstance> parkedVehicles) {
    this.track = track;
    this.availableSpace = track.getTrackLength() - TrackUtils.parkLength(parkedVehicles);
    this.parkedVehicles = new ArrayList<>(parkedVehicles);
    this.allowedVehicleSeries = parkedVehicles.isEmpty()
        ? null : parkedVehicles.get(0).getVehicle().getSeries();
  }

  public Integer getAllowedVehicleSeries() {
    return allowedVehicleSeries;
  }

  public Track getTrack() {
    return track;
  }

  boolean add(VehicleInstance vehicleInstance) {
    assert canAdd(vehicleInstance.getVehicle());

    // condition (7): vehicles should be sorted in departing order
    return insertSortedByDeparture(vehicleInstance);
  }

  public boolean canAdd(Vehicle vehicle) {
    return availableSpace >= vehicle.getVehicleLength() + deltaSpaceForNextVehicle()
        && track.getAllowedVehicleIds().contains(vehicle.getId())
        && (allowedVehicleSeries == null || allowedVehicleSeries == vehicle.getSeries());
  }

  VehicleInstance popRandom(Random random) {
    if (nParkedVehicles() == 0) {
      return null;
    }
    return pop(random.nextInt(nParkedVehicles()));
  }

  VehicleInstance pop(int index) {
    VehicleInstance vehicleInstance = parkedVehicles.remove(index);
    availableSpace += vehicleInstance.getVehicle().getVehicleLength();
    if (parkedVehicles.size() >= 1) {
      availableSpace += TrackUtils.SPACE_BETWEEN_CARS;
    }
    if (parkedVehicles.isEmpty()) {
      allowedVehicleSeries = null;
    }
    return vehicleInstance;
  }

  public int nParkedVehicles() {
    return parkedVehicles.size();
  }

  /** @return immutable view of parked vehicles, sorted by departure time */
  public List<VehicleInstance> getParkedVehicles() {
    // condition (7): vehicles should be sorted in departing order
    // NOTE: this is currently handled from within #add
//    parkedVehicles.sort(Comparator.comparingInt(vi -> vi.getVehicle().getDeparture()));
    return Collections.unmodifiableList(parkedVehicles);
  }

  boolean setParkedVehicles(List<VehicleInstance> vehicleInstances) {
    double parkingLength = TrackUtils.parkLength(vehicleInstances);
    if (parkingLength > track.getTrackLength()
        || !TrackUtils.allIdsAllowed(vehicleInstances, track)) {
      return false;
    }

    parkedVehicles = vehicleInstances;
    availableSpace = track.getTrackLength() - parkingLength;
    allowedVehicleSeries = vehicleInstances.isEmpty() ?
        null : vehicleInstances.get(0).getVehicle().getSeries();
    return true;
  }

  @Override
  public String toString() {
    return getParkedVehicles().stream()
        .map(VehicleInstance::getVehicle)
        .map(Vehicle::getId)
        .map(Object::toString)
        .collect(Collectors.joining(" "));
  }

  /** assumes all validity checks have already been performed. */
  private boolean insertSortedByDeparture(VehicleInstance vehicleInstance) {
    Vehicle vehicleToInsert = vehicleInstance.getVehicle();

    int i = 0;
    for (; i < parkedVehicles.size(); i++) {
      Vehicle parkedVehicle = parkedVehicles.get(i).getVehicle();
      if (parkedVehicle.getDeparture() >= vehicleToInsert.getDeparture()) {
        break;
      }
    }

    parkedVehicles.add(i, vehicleInstance);
    availableSpace -= (vehicleToInsert.getVehicleLength() + deltaSpaceForNextVehicle());
    allowedVehicleSeries = vehicleToInsert.getSeries();
    return true;
  }

  private double deltaSpaceForNextVehicle() {
    return parkedVehicles.isEmpty() ? 0 : TrackUtils.SPACE_BETWEEN_CARS;
  }
}
