package hmo;

import hmo.common.TrackUtils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Evaluator {

  private SolutionInstance solutionInstance;

  public Evaluator(SolutionInstance solutionInstance) {
    this.solutionInstance = solutionInstance;
  }

  public double fitnessToMaximize() {
    solutionInstance.resetVehiclePool();

    double minimizationGoal = p1f1() + p2f2() + p3f3();
    double maximizationGoal = r1g1() + r2g2() + r3g3();
    maximizationGoal = Double.isFinite(maximizationGoal) ? maximizationGoal : 0;
    minimizationGoal = Double.isFinite(minimizationGoal) ? minimizationGoal : Double.MAX_VALUE;
    double numUsed = solutionInstance.getAssignedVehicles().size();
    double numVehicles = solutionInstance.getProblem().getVehicles().size();
    double numBlockers = blockerCount();
    double usedVehiclesGoal = Math.pow(1.5, numUsed / numVehicles);
    double blockerGoal = Math.pow(1.5, numVehicles / (numBlockers + 1));
    double result =
        1.0 / (minimizationGoal + 1)
        + maximizationGoal
        + usedVehiclesGoal
        + blockerGoal;
    if (Double.isInfinite(result)) {
      System.out.println("INF");
    }
    return result;
  }

  private double blockerCount() {
    double count = 0;
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

        List<VehicleInstance> parkedVehicles = blockingInstance
            .getParkedVehicles();
        for (int i = parkedVehicles.size() - 1; i >= 0; i--) {
          int lastDeparture = parkedVehicles
              .get(i)
              .getVehicle()
              .getDeparture();
          if (lastDeparture >= firstDeparture) {
            count++;
          } else {
            break;
          }
        }
      }
    }

    return count;
  }

  private double p1f1() {
    int f1 = 0;
    boolean first = true;
    int previousType = -1;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      List<VehicleInstance> veh = track.getParkedVehicles();
      if (veh.isEmpty()) {
        continue;
      }

      int temp = veh.get(0).getVehicle().getSeries();
      if (first) {
        first = false;
        previousType = temp;
      } else {
        if (temp != previousType) {
          f1++;
        }
        previousType = temp;
      }
    }
    //System.out.println("p1f1=" + Math.pow(solutionInstance.nUsedTracks() - 1, -1) * (double) f1);
    int usedTracksOr1 = Math.max(1, solutionInstance.nUsedTracks() - 1);
    return Math.pow(solutionInstance.nUsedTracks() - 1, -1) * (double) f1;
  }

  private double p2f2() {
    int nTotalTracks = solutionInstance.getProblem().getTracks().size();
    //System.out.println("p2f2=" + Math.pow(nTotalTracks, -1) * solutionInstance.nUsedTracks());
    return Math.pow(nTotalTracks, -1) * solutionInstance.nUsedTracks();
  }

  private double p3f3() {
    int totalTrackLength = solutionInstance.getProblem().getTotalTrackLength();
    int totalVehicleLength = solutionInstance.getProblem().getTotalVehicleLength();
    double f3 = 0;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      if (!track.getParkedVehicles().isEmpty()) {
        f3 += track.getTrack().getTrackLength() - TrackUtils.parkLength(track.getParkedVehicles());
      }
    }
    //System.out.println("p3f3=" + Math.pow(totCap - totLen, -1) * f3);
    return Math.pow(totalTrackLength - totalVehicleLength, -1) * f3;
  }

  private double r1g1() {
    int r1 =
        solutionInstance.getProblem().getVehicles().size() - solutionInstance.nUsedTracks();
    int g1 = 0;
    for (TrackInstance track : solutionInstance.getTrackInstances()) {
      boolean first = true;
      int previousType = -1;
      List<VehicleInstance> veh = track.getParkedVehicles();
      if (!veh.isEmpty()) {
        for (VehicleInstance vehicle : veh) {
          int temp = vehicle.getVehicle().getLayoutType();
          if (first) {
            first = false;
            previousType = temp;
          } else {
            if (temp == previousType) {
              g1++;
            }
            previousType = temp;
          }
        }
      }
    }
    //System.out.println("r1g1=" + Math.pow(r1, -1) * (double) g1);
    return Math.pow(r1, -1) * (double) g1;
  }

  private double r2g2() {
    int r2 = solutionInstance.nUsedTracks() - 1;
    int g2 = 0;

    boolean first = true;
    int previousType = -1;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      if (!track.getParkedVehicles().isEmpty()) {
        if (first) {
          first = false;
          previousType = track.getParkedVehicles().get(track.getParkedVehicles().size() - 1)
              .getVehicle().getLayoutType();
        } else {
          int temp = track.getParkedVehicles().get(0).getVehicle().getLayoutType();
          if (temp == previousType) {
            g2++;
          }
          previousType = temp;
        }
      }
    }
    //System.out.println("r2g2=" + Math.pow(r2, -1) * (double) g2);
    int r2Or1 = Math.max(1, r2);
    return Math.pow(r2, -1) * (double) g2;
  }

  private double r3g3() {
    int r3 = 0;
    int g3 = 0;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      int size = track.getParkedVehicles().size();
      for (int i = 0; i < size - 1; i++) {
        r3 += 15;
        int firstStart = track.getParkedVehicles().get(i).getVehicle().getDeparture();
        int secondStart = track.getParkedVehicles().get(i + 1).getVehicle().getDeparture();
        int diff = secondStart - firstStart;
        if (diff < 10) {
          g3 -= 4 * (10 - diff);
        } else if (diff <= 20) {
          g3 += 15;
        } else {
          g3 += 10;
        }
      }
    }

    // 0^-1 is infinity
    int r3or1 = Math.max(1, r3);
    return Math.pow(r3or1, -1) * (double) g3;
  }
}
