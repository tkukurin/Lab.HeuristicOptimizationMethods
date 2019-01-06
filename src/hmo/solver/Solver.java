package hmo.solver;

import hmo.instance.SolutionInstance;
import hmo.problem.Problem;

public abstract class Solver {

  protected Problem problem;

  public Solver(Problem problem) {
    this.problem = problem;
  }

  public abstract SolutionInstance solve();

}
