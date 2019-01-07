package hmo;

import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import java.util.List;

public class Evaluator {

  private SolutionInstance solutionInstance;

  public Evaluator(SolutionInstance solutionInstance) {
    this.solutionInstance = solutionInstance;
  }

  public double rate() {
    double firstGoal = p1f1() + p2f2() + p3f3();
    double secondGoal = r1g1() + r2g2() + r3g3();
    // TODO this is just temporary
    return secondGoal;
  }

  private double p1f1() {
    int f1 = 0;
    boolean first = true;
    int previousType = -1;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      List<VehicleInstance> veh = track.getParkedVehicles();
      if (!veh.isEmpty()) {
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
    }
    //System.out.println("p1f1=" + Math.pow(solutionInstance.nUsedTracks() - 1, -1) * (double) f1);
    return Math.pow(solutionInstance.nUsedTracks() - 1, -1) * (double) f1;
  }

  private double p2f2() {
    int nTotalTracks = solutionInstance.getProblem().getTracks().size();
    //System.out.println("p2f2=" + Math.pow(nTotalTracks, -1) * solutionInstance.nUsedTracks());
    return Math.pow(nTotalTracks, -1) * solutionInstance.nUsedTracks();
  }

  private double p3f3() {
    int totCap = solutionInstance.getProblem().getTotalTrackLength();
    int totLen = solutionInstance.getProblem().getTotalVehicleLength();
    double f3 = 0.;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      double tot = (double) track.getTrack().getTrackLength();
      double used = 0.;
      for (VehicleInstance vehicle : track.getParkedVehicles()) {
        used += (double) vehicle.getVehicle().getVehicleLength();
      }
      used += 0.5 * (double) track.getParkedVehicles().size() - 1;
      if (used > 0.) {
        f3 += tot - used;
      }
    }
    //System.out.println("p3f3=" + Math.pow(totCap - totLen, -1) * f3);
    return Math.pow(totCap - totLen, -1) * f3;
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
    return Math.pow(r2, -1) * (double) g2;
  }

  private double r3g3() {
    int r3 = 0;
    int g3 = 0;
    for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
      if (!track.getParkedVehicles().isEmpty()) {
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
    }
    //System.out.println("r3g3=" + Math.pow(r3, -1) * (double) g3);
    return Math.pow(r3, -1) * (double) g3;
  }
}
