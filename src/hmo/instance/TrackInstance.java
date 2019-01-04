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

  public boolean canAdd(Vehicle vehicle) {
    return availableSpace >= vehicle.getVehicleLength()
        && track.getAllowedVehicleIds().contains(vehicle.getId())
        && (allowedVehicleSeries == null || allowedVehicleSeries == vehicle.getSeries());
  }

  public boolean add(VehicleInstance vehicleInstance) {
    Vehicle vehicle = vehicleInstance.getVehicle();
    assert canAdd(vehicle);

    parkedVehicles.add(vehicleInstance);
    availableSpace -= vehicle.getVehicleLength();
    allowedVehicleSeries = vehicle.getSeries();
    return true;
  }

  /** @return parked vehicles **in order** */
  public List<VehicleInstance> getParkedVehicles() {
    return parkedVehicles;
  }

  // no equals or hashcode for now.

  @Override
  public String toString() {
    return parkedVehicles.stream()
        .map(VehicleInstance::getVehicle)
        .map(Vehicle::getId)
        .map(Object::toString)
        .collect(Collectors.joining(" "));
  }
}
