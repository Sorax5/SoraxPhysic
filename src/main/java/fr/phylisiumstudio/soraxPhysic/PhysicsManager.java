package fr.phylisiumstudio.soraxPhysic;

import fr.phylisiumstudio.logic.IPhysicsManager;
import fr.phylisiumstudio.logic.WorldManager;
import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PhysicsManager extends IPhysicsManager {
    private final WorldManager worldManager;
    private final Logger logger = LoggerFactory.getLogger(PhysicsManager.class);
    public static final Object lock = new Object();


    /**
     * Create a new physics manager
     */
    public PhysicsManager() {
        this.worldManager = new WorldManager();
    }

    /**
     * Register a world with the physics engine
     *
     * @param world The world to register
     */
    @Override
    public void registerWorld(WorldPhysics world) {
        this.worldManager.registerWorld(world);
    }

    /**
     * Unregister a world from the physics engine
     * @param world The world to unregister
     */
    @Override
    public void unregisterWorld(WorldPhysics world) {
        this.worldManager.unregisterWorld(world);
    }

    /**
     * Create a box
     * @param location The location of the box
     * @param data The block data
     * @param mass The mass of the box
     * @param xScale The x scale of the box
     * @param yScale The y scale of the box
     * @param zScale The z scale of the box
     * @return The rigid block
     */
    @Override
    public IRigidBlock createBox(Location location, BlockData data, float mass, float xScale, float yScale, float zScale) {
        WorldPhysics world = getWorldPhysics(location.getWorld().getUID());
        if (world == null) {
            throw new IllegalArgumentException("The world is not managed by the physics engine");
        }
        return world.createBox(location, data, mass, xScale, yScale, zScale);
    }

    /**
     * Create a sphere
     * @param location The location of the sphere
     * @param data The block data
     * @param mass The mass of the sphere
     * @param radius The radius of the sphere
     */
    @Override
    public void createSphere(Location location, BlockData data, float mass, float radius) {
        WorldPhysics world = getWorldPhysics(location.getWorld().getUID());
        if (world == null) {
            throw new IllegalArgumentException("The world is not managed by the physics engine");
        }
        world.createSphere(location, data, radius, mass);
    }

    /**
     * Get the physics object for the given world
     * @param uniqueId The unique id of the world
     * @return The physics object or null if not found
     */
    @Override
    public WorldPhysics getWorldPhysics(UUID uniqueId) {
        return worldManager.getWorld(uniqueId);
    }

    /**
     * Clear all the physics objects
     */
    @Override
    public void clear() {
        worldManager.getWorlds().forEach((id, world) -> {
            world.clear();
        });
    }

    /**
     * Stop the physics engine
     */
    @Override
    public void stop() {
        worldManager.shutdown();
    }

    /**
     * Pause the physics simulation for all worlds
     */
    @Override
    public void pauseAllWorlds() {
        worldManager.getWorlds().forEach((id, world) -> {
            world.setFreeze(true);
        });
    }

    /**
     * Resume the physics simulation for all worlds
     */
    @Override
    public void resumeAllWorlds() {
        worldManager.getWorlds().forEach((id, world) -> {
            world.setFreeze(false);
        });
    }

    /**
     * Get all the worlds being simulated
     * @return The list of worlds
     */
    @Override
    public List<WorldPhysics> getAllWorlds() {
        return new ArrayList<>(worldManager.getWorlds().values());
    }

    /**
     * Check if a world is being simulated
     * @param worldId The unique id of the world
     * @return True if the world is being simulated
     */
    @Override
    public boolean isWorldSimulated(UUID worldId) {
        return worldManager.isWorldRegistered(worldId);
    }

}
