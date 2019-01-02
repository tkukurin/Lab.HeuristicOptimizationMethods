package hmo.instance;

import hmo.problem.Track;
import hmo.problem.Vehicle;

public class VehicleInstance {

  private Vehicle vehicle;
  private Track track;

  public VehicleInstance(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public VehicleInstance(Vehicle vehicle, Track track) {
    this.vehicle = vehicle;
    this.track = track;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public Track getTrack() {
    return track;
  }
}

