package no.ntnu.listeners.controlpanel;

import no.ntnu.greenhouse.Actuator;

public interface ActuatorChangedListener {

    /**
     * Called when an actuator has changed its state.
     *
     * @param nodeId The ID of the node where the actuator is located.
     * @param actuatorId The ID of the actuator that has changed.
     * @param state The new state of the actuator.
     */
    void onActuatorChanged(int nodeId, int actuatorId, boolean state);
}
