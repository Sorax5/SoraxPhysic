package fr.phylisiumstudio.logic;

import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manage the worlds and the threads associated with them
 */
public class WorldManager {
    private final ConcurrentMap<UUID, WorldPhysics> worlds;
    private final PhysicsThreadManager physicsThreadManager;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(WorldManager.class);

    public WorldManager() {
        this.worlds = new ConcurrentHashMap<>();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("Available processors: {}", availableProcessors);
        this.physicsThreadManager = new PhysicsThreadManager(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Register a world with the physics engine
     * @param world The world to register
     */
    public void registerWorld(WorldPhysics world) {
        if (world == null) {
            throw new IllegalArgumentException("The world or its UID is null");
        }
        if (worlds.containsKey(world.getUniqueId())) {
            throw new IllegalArgumentException("The world is already registered");
        }

        worlds.put(world.getUniqueId(), world);
        startWorldPhysics(world);
    }

    /**
     * Unregister a world from the physics engine
     * @param world The world to unregister
     */
    public void unregisterWorld(WorldPhysics world) {
        if (world == null) {
            throw new IllegalArgumentException("The world or its UID is null");
        }
        if (!worlds.containsKey(world.getUniqueId())) {
            throw new IllegalArgumentException("The world is not registered");
        }

        WorldPhysics worldPhysics = worlds.remove(world.getUniqueId());
        physicsThreadManager.unregisterWorld(worldPhysics.getUniqueId());
    }

    /**
     * Start the physics for a world
     * @param worldPhysics The world physics
     */
    public void startWorldPhysics(WorldPhysics worldPhysics) {
        if (worldPhysics == null) {
            throw new IllegalArgumentException("The world physics is null");
        }
        if (!worlds.containsKey(worldPhysics.getUniqueId())) {
            throw new IllegalArgumentException("The world is not registered");
        }

        physicsThreadManager.registerWorld(worldPhysics);
    }

    /**
     * Get the world physics associated with the world
     * @param worldId The world id
     * @return The world physics
     */
    public WorldPhysics getWorld(UUID worldId) {
        return worlds.get(worldId);
    }

    /**
     * Check if a world is registered
     * @param worldId The world id
     * @return True if the world is registered
     */
    public boolean isWorldRegistered(UUID worldId) {
        return worlds.containsKey(worldId);
    }

    /**
     * Shutdown the world manager
     */
    public void shutdown() {
        physicsThreadManager.shutdown();
        worlds.clear();
    }

    public PhysicsThreadManager getPhysicsThreadManager() {
        return physicsThreadManager;
    }

    /**
     * Get all the worlds being managed
     * @return a copy of the worlds
     */
    public ConcurrentMap<UUID, WorldPhysics> getWorlds() {
        return new ConcurrentHashMap<>(worlds);
    }
}
