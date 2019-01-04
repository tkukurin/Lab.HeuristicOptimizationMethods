package hmo.instance;

import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.ArrayList;
import java.util.List;
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

  public Track getTrack() {
    return track;
  }

  public boolean add(VehicleInstance vehicleInstance) {
    Vehicle vehicle = vehicleInstance.getVehicle();
    assert canAdd(vehicle);

    // condition (7): vehicles should be sorted in departing order
    insertSorted(vehicleInstance);
    return true;
  }

  public boolean canAdd(Vehicle vehicle) {
    return availableSpace >= vehicle.getVehicleLength()
        && track.getAllowedVehicleIds().contains(vehicle.getId())
        && (allowedVehicleSeries == null || allowedVehicleSeries == vehicle.getSeries());
  }

  /** @return parked vehicles **in order** (sorted by departure time) */
  public List<VehicleInstance> getParkedVehicles() {
    // condition (7): vehicles should be sorted in departing order
    // NOTE: this is currently handled from within #add
//    parkedVehicles.sort(Comparator.comparingInt(vi -> vi.getVehicle().getDeparture()));
    return parkedVehicles;
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

  private void insertSorted(VehicleInstance vehicleInstance) {
    Vehicle vehicleToInsert = vehicleInstance.getVehicle();

    int i;
    for (i = 0; i < parkedVehicles.size(); i++) {
      Vehicle parkedVehicle = parkedVehicles.get(i).getVehicle();
      if (parkedVehicle.getDeparture() >= vehicleToInsert.getDeparture()) {
        break;
      }
    }

    parkedVehicles.add(i, vehicleInstance);
    availableSpace -= vehicleToInsert.getVehicleLength();
    allowedVehicleSeries = vehicleToInsert.getSeries();
  }
}
