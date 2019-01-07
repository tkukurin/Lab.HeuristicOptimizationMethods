package hmo;

import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;

import java.util.ArrayList;
import java.util.List;

public class RestrictionsHelper {

    private SolutionInstance solutionInstance;

    public RestrictionsHelper(SolutionInstance solutionInstance) {
        this.solutionInstance = solutionInstance;
    }

    public boolean singleTrackTest() {
        // test not needed, vehicleInstance can have just one Track
        return true;
    }

    public boolean singleSeriesInTracksTest() {
        for (TrackInstance track : solutionInstance.getTrackInstances()) {
            if (!track.getParkedVehicles().isEmpty()) {
                int series = -1;
                boolean first = true;
                for (VehicleInstance vehicle : track.getParkedVehicles()) {
                    if (first) {
                        first = false;
                        series = vehicle.getVehicle().getSeries();
                    } else {
                        if (vehicle.getVehicle().getSeries() != series) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean vehiclesAllowedInAssignedTracksTest() {
        for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
            List<Integer> allowedVehicles = (List<Integer>) track.getTrack().getAllowedVehicleIds();
            for (VehicleInstance vehicle : track.getParkedVehicles()) {
                int vehicleOrder = vehicle.getVehicle().getId();
                if (!allowedVehicles.contains(vehicleOrder)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean tracksNotOverloadedTest() {
        for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
            double trackLenLeft = (double) track.getTrack().getTrackLength();
            for (VehicleInstance vehicle : track.getParkedVehicles()) {
                trackLenLeft -= ((double) vehicle.getVehicle().getVehicleLength() + 0.5);
            }
            // one gap between vehicles too many has to be removed
            trackLenLeft += 0.5;
            if (trackLenLeft < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean vehicleOnlyOnceInATrackTest() {
        for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
            List<VehicleInstance> vehiclesInATrack = new ArrayList<>();
            for (VehicleInstance vehicle : track.getParkedVehicles()) {
                if (vehiclesInATrack.contains(vehicle)) {
                    return false;
                }
                vehiclesInATrack.add(vehicle);
            }
        }
        return true;
    }

    public boolean singlePositionInATrackTest() {
        // we don't assign positions to vehicles
        return true;
    }

    public boolean orderOfDepartureOfVehiclesInTheSameTrackTest() {
        for (TrackInstance track : solutionInstance.getTrackInstancesInorder()) {
            int departure = -1;
            for (VehicleInstance vehicle : track.getParkedVehicles()) {
                int nextDeparture = vehicle.getVehicle().getDeparture();
                if (nextDeparture <= departure) {
                    return false;
                }
                departure = nextDeparture;
            }
        }
        return true;
    }

    public boolean vehiclesInBlockedTracksDepartureTimesTest() {
        List<TrackInstance> allTracks = solutionInstance.getTrackInstancesInorder();
        for (int i = 0; i < allTracks.size(); i++) {
            int firstDeparture = allTracks.get(i).getParkedVehicles().get(0).getVehicle().getDeparture();
            List<Integer> blockedBy = (List<Integer>) solutionInstance.getProblem().getBlockedBy(i);
            for (Integer n : blockedBy) {
                int len = allTracks.get(n).getParkedVehicles().size();
                int lastDeparture = allTracks.get(n).getParkedVehicles().get(len - 1).getVehicle().getDeparture();
                if (lastDeparture >= firstDeparture) {
                    return false;
                }
            }
        }
        return true;
    }
}
