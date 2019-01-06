package hmo.instance;

import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TrackInstance {

  private Track track;
  private int availableSpace;
  private Integer allowedVehicleSeries;
  private List<VehicleInstance> parkedVehicles;

  public TrackInstance(Track track) {
    this.track = track;
    this.availableSpace = track.getTrackLength();
    this.parkedVehicles = new ArrayList<>();
    this.allowedVehicleSeries = null;
  }

  public TrackInstance(Track track, List<VehicleInstance> parkedVehicles) {
    this.track = track;
    this.availableSpace =
        track.getTrackLength() - parkedVehicles.stream().map(v -> v.getVehicle().getVehicleLength())
            .reduce((i, j) -> i + j).orElse(0);
    this.parkedVehicles = parkedVehicles;
    this.allowedVehicleSeries = parkedVehicles.isEmpty()
        ? null : parkedVehicles.get(0).getVehicle().getSeries();
  }

  public Integer getAllowedVehicleSeries() {
    return allowedVehicleSeries;
  }

  public Track getTrack() {
    return track;
  }

  public boolean add(VehicleInstance vehicleInstance) {
    assert canAdd(vehicleInstance.getVehicle());

    // condition (7): vehicles should be sorted in departing order
    return insertSortedByDeparture(vehicleInstance);
  }

  public boolean canAdd(Vehicle vehicle) {
    return availableSpace >= vehicle.getVehicleLength()
        && track.getAllowedVehicleIds().contains(vehicle.getId())
        // a track must have only one type of series
        && (allowedVehicleSeries == null || allowedVehicleSeries == vehicle.getSeries());
  }

  public VehicleInstance popRandom(Random random) {
    VehicleInstance vehicleInstance = pop(random.nextInt(nParkedVehicles()));
    return vehicleInstance;
  }

  public VehicleInstance pop(int index) {
    VehicleInstance vehicleInstance = parkedVehicles.remove(index);
    availableSpace += vehicleInstance.getVehicle().getVehicleLength();
    if (parkedVehicles.isEmpty()) {
      allowedVehicleSeries = null;
    }
    return vehicleInstance;
  }

  public int nParkedVehicles() {
    return parkedVehicles.size();
  }

  /** @return parked vehicles, sorted by departure time */
  public List<VehicleInstance> getParkedVehicles() {
    // condition (7): vehicles should be sorted in departing order
    // NOTE: this is currently handled from within #add
//    parkedVehicles.sort(Comparator.comparingInt(vi -> vi.getVehicle().getDeparture()));
    return Collections.unmodifiableList(parkedVehicles);
  }

  public void setParkedVehicles(List<VehicleInstance> unusedCurrent) {
    parkedVehicles = unusedCurrent;
  }

  // no equals or hashcode for now.

  @Override
  public String toString() {
    return getParkedVehicles().stream()
        .map(VehicleInstance::getVehicle)
        .map(Vehicle::getId)
        .map(Object::toString)
        .collect(Collectors.joining(" "));
  }

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
    availableSpace -= vehicleToInsert.getVehicleLength();
    allowedVehicleSeries = vehicleToInsert.getSeries();
    return true;
  }
}
