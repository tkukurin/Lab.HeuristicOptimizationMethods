package hmo.instance;

import hmo.problem.Problem;
import java.util.List;
import java.util.stream.Collectors;

public class SolutionInstance {

  private Problem problem;
  private List<TrackInstance> trackInstances;
  private List<VehicleInstance> vehicleInstances;

  public SolutionInstance(Problem problem) {
    this.problem = problem;
    this.vehicleInstances = problem.getVehicles().stream().map(VehicleInstance::new)
        .collect(Collectors.toList());
    this.trackInstances = problem.getTracks().stream().map(TrackInstance::new)
        .collect(Collectors.toList());
  }

  public Problem getProblem() {
    return problem;
  }

  public List<TrackInstance> getTrackInstances() {
    return trackInstances;
  }

  public List<VehicleInstance> getVehicleInstances() {
    return vehicleInstances;
  }
}
