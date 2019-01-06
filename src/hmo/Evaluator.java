package hmo;

import hmo.instance.SolutionInstance;
import hmo.problem.Problem;

public class Evaluator {

  private Problem problem;

  public Evaluator(Problem problem) {
    this.problem = problem;
  }

  public double rate(SolutionInstance solutionInstance) {
    // TODO evaluator rates how good the solution is
    return 0.0;
  }

  private double p2f2(int nUsedTracks) {
    int nTotalTracks = problem.getTracks().size();
    return Math.pow(nTotalTracks, -1) * nUsedTracks;
  }

  private double p3f3() {
    return 0.0;
  }
}
