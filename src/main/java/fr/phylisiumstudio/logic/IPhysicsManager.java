package fr.phylisiumstudio.logic;

import org.bukkit.World;

/**
 * Interface for the physics manager
 */
public interface IPhysicsManager {
    /**
     * Get the physics engine for a world
     * @param world The world
     * @return The physics engine
     */
    IWorldPhysics getWorldPhysics(World world);

    /**
     * Setup the physics engine for a world
     * @param world The world
     */
    void setupPhysicsEngine(World world);

    /**
     * Stop the physics engine
     */
    void stop();
}
