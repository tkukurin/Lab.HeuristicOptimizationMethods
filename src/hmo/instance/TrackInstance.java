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

  public boolean addVehicle(VehicleInstance vehicleInstance) {
    Vehicle vehicle = vehicleInstance.getVehicle();
    if (availableSpace > vehicle.getVehicleLength() && track.getRestrictions().get(vehicle.getOrd()) == 1) {
      parkedVehicles.add(vehicleInstance);
      this.availableSpace -= vehicle.getVehicleLength();
      return true;
    } else {
      return false;
    }
  }

  // no equals or hashcode for now.

  public String printVehiclesInTrack() {
    return parkedVehicles.stream()
        .map(VehicleInstance::getVehicle)
        .map(Vehicle::getOrd)
        .map(Object::toString)
        .collect(Collectors.joining(" "));
  }

}
