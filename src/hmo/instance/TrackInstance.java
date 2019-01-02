package hmo.instance;

import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.stream.Collectors;

public class TrackInstance {

  private Track track;
  private int availableSpace;
  private Collection<VehicleInstance> parkedVehicles;

  public TrackInstance(Track track) {
    this.track = track;
  }

  public boolean canAdd(Vehicle vehicle) {
    return availableSpace >= vehicle.getVehicleLength()
        && track.getAllowedVehicleTypes().contains(vehicle.getId());
  }

  public boolean add(VehicleInstance vehicleInstance) {
    Vehicle vehicle = vehicleInstance.getVehicle();
    if (this.canAdd(vehicle)) {
      parkedVehicles.add(vehicleInstance);
      availableSpace -= vehicle.getVehicleLength();
      return true;
    }

    return false;
  }

  public Collection<VehicleInstance> getParkedVehicles() {
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
