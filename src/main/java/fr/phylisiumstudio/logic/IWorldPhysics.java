package fr.phylisiumstudio.logic;

import java.util.UUID;

/**
 * Interface for the physics world
 */
public interface IWorldPhysics {
    /**
     * Step the simulation
     */
    void stepSimulation();

    /**
     * Get the unique id of the world
     * @return the unique id
     */
    UUID uniqueId();
}
