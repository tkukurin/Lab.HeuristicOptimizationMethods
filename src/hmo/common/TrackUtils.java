package hmo.common;

import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.List;

public class TrackUtils {
  public static final double SPACE_BETWEEN_CARS = 0.5;

  private TrackUtils() {}

  public static double parkLength(Collection<VehicleInstance> vehicles) {
    int vehicleLens = vehicles.stream()
        .map(VehicleInstance::getVehicle)
        .mapToInt(Vehicle::getVehicleLength)
        .sum();
    double spaces = Math.max(0, vehicles.size() - 1) * SPACE_BETWEEN_CARS;
    return vehicleLens + spaces;
  }

  public static boolean allIdsAllowed(List<VehicleInstance> vehicleInstances, Track track) {
    return vehicleInstances.stream()
        .map(VehicleInstance::getVehicle)
        .map(Vehicle::getId)
        .allMatch(id -> track.getAllowedVehicleIds().contains(id));
  }

  public static boolean validSeries(Vehicle vehicle, TrackInstance trackInstance) {
    return trackInstance.getAllowedVehicleSeries() == null
        || trackInstance.getAllowedVehicleSeries() == vehicle.getSeries();
  }

  public static boolean validId(Vehicle vehicle, Track track) {
    return track.getAllowedVehicleIds().contains(vehicle.getId());
  }

  public static boolean canSetVehicles(List<VehicleInstance> vehicles, Track track) {
    return allIdsAllowed(vehicles, track)
        && parkLength(vehicles) <= track.getTrackLength();
  }

}
