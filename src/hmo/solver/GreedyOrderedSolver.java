package hmo.solver;

import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GreedyOrderedSolver extends Solver {

  private static final Logger LOG = Logger.getLogger(GreedyOrderedSolver.class.toString());

  private Random random;

  public GreedyOrderedSolver(Problem problem, Random random) {
    super(problem);
    this.random = random;
  }

  @Override
  public SolutionInstance solve() {
    SolutionInstance solutionInstance = new SolutionInstance(problem);

    LOG.info("Starting greedy algorithm.");
    solutionInstance.getProblem().getVehicles().stream()
        .sorted(Comparator.comparingInt(Vehicle::getDeparture))
        .forEach(vehicle -> {
          Set<Integer> triedIds = new HashSet<>();
          Map<Integer, Track> idToTrack =
              solutionInstance.tracksForVehicle(vehicle).collect(
                  Collectors.toMap(Track::getId, t -> t));
          Track chosenTrack = idToTrack.values().stream().findFirst().orElse(null);
          while (chosenTrack != null) {
            triedIds.add(chosenTrack.getId());
            Collection<Integer> trackBlockedBy = problem.getBlockedBy(chosenTrack.getId());
            Integer nextChosenId = null;
            for (int blockId : trackBlockedBy) {
              if (idToTrack.containsKey(blockId) && !triedIds.contains(blockId)) {
                nextChosenId = blockId;
                break;
              }
            }

            if (nextChosenId == null) {
              solutionInstance.assign(vehicle, chosenTrack);
              break;
            }

            chosenTrack = idToTrack.get(nextChosenId);
          }
        });

    LOG.info("Completed greedy ordered algorithm.");
    LOG.info(String.format("Assigned %s cars.", solutionInstance.getVehicleInstances().size()));
    solutionInstance.resetVehiclePool();
    return solutionInstance;
  }

}
