package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.model.Drone;

public class Follow implements OnDroneListener, LocationReceiver {

    /**
     * Set of return value for the 'toggleFollowMeState' method.
     */
    public enum FollowStates {
        FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
    }

    private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
    private Drone drone;

    private LocationFinder locationFinder;
    private FollowAlgorithm followAlgorithm;

    public Follow(Drone drone, Handler handler, LocationFinder locationFinder) {
        this.drone = drone;
        drone.addDroneListener(this);

        followAlgorithm = FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(drone, handler);

        this.locationFinder = locationFinder;
        locationFinder.setLocationListener(this);
    }

    public void toggleFollowMeState() {
        final State droneState = drone.getState();
        if (droneState == null) {
            state = FollowStates.FOLLOW_INVALID_STATE;
            return;
        }

        if (isEnabled()) {
            disableFollowMe();
        } else {
            if (drone.isConnected()) {
                if (droneState.isArmed()) {
                    GuidedPoint.changeToGuidedMode(drone);
                    enableFollowMe();
                } else {
                    state = FollowStates.FOLLOW_DRONE_NOT_ARMED;
                }
            } else {
                state = FollowStates.FOLLOW_DRONE_DISCONNECTED;
            }
        }
    }

    private void enableFollowMe() {
        followAlgorithm.enableFollow();
        locationFinder.enableLocationUpdates();

        state = FollowStates.FOLLOW_START;
        drone.notifyDroneEvent(DroneEventsType.FOLLOW_START);
    }

    private void disableFollowMe() {
        followAlgorithm.disableFollow();
        locationFinder.disableLocationUpdates();

        if (isEnabled()) {
            state = FollowStates.FOLLOW_END;
            drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
        }
    }

    public boolean isEnabled() {
        return state == FollowStates.FOLLOW_RUNNING || state == FollowStates.FOLLOW_START;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case MODE:
                if (!GuidedPoint.isGuidedMode(drone)) {
                    disableFollowMe();
                }
                break;

            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                disableFollowMe();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.isAccurate()) {
            state = FollowStates.FOLLOW_RUNNING;
            followAlgorithm.processNewLocation(location);
        } else {
            state = FollowStates.FOLLOW_START;
        }

        drone.notifyDroneEvent(DroneEventsType.FOLLOW_UPDATE);
    }

    public void setAlgorithm(FollowAlgorithm algorithm) {
        if(followAlgorithm != null && followAlgorithm != algorithm){
            followAlgorithm.disableFollow();
        }

        followAlgorithm = algorithm;
        if(isEnabled()){
            followAlgorithm.enableFollow();
        }
        drone.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
    }

    public FollowAlgorithm getFollowAlgorithm() {
        return followAlgorithm;
    }

    public FollowStates getState() {
        return state;
    }
}
