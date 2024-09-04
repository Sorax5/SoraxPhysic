package fr.phylisiumstudio.soraxPhysic;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.*;
import fr.phylisiumstudio.bullet.BulletWorldPhysics;
import fr.phylisiumstudio.logic.ThreadManager;
import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class PhysicsManager {
    private final List<WorldPhysics> worlds;
    private final ThreadManager threadManager;
    public static final Object lock = new Object();
    private final Logger logger = LoggerFactory.getLogger(PhysicsManager.class);

    private boolean timeFreeze = false;
    private boolean running = true;


    /**
     * Create a new physics manager
     */
    public PhysicsManager() {
        this.worlds = new ArrayList<>();

        int maxThreads = Runtime.getRuntime().availableProcessors();
        this.threadManager = new ThreadManager(maxThreads);

        Bukkit.getScheduler().runTaskTimerAsynchronously(SoraxPhysic.getInstance(), this::checkInactiveWorlds, 0L, 100L);
    }

    private void checkInactiveWorlds() {
        synchronized (lock) {
            for (WorldPhysics worldPhysics : worlds) {
                World world = Bukkit.getWorld(worldPhysics.getUniqueId());
                if (world != null && world.getPlayers().isEmpty()) {
                    stopWorldPhysics(worldPhysics);
                } else if (world != null && !world.getPlayers().isEmpty()) {
                    startWorldPhysics(worldPhysics);
                }
            }
        }
    }

    private void runWorldPhysics(WorldPhysics worldPhysics) {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                synchronized (lock) {
                    if (!timeFreeze) {
                        worldPhysics.stepSimulation();
                    }
                }
                Thread.sleep(Duration.ofMillis(50));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Physics thread interrupted for world: " + worldPhysics.getUniqueId());
        } catch (Exception e) {
            logger.error("Error in world physics thread", e);
        }
    }

    /**
     * Register a world with the physics engine
     * @param world The world to register
     * @return The physics object for the world
     */
    public WorldPhysics registerWorld(World world) {
        synchronized (lock) {
            if (world == null) {
                throw new IllegalArgumentException("The world or its UID is null");
            }

            if (getWorldPhysics(world.getUID()) != null) {
                throw new IllegalArgumentException("The world is already managed by the physics engine");
            }

            BroadphaseInterface broadphase = new DbvtBroadphase();
            CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
            CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
            ConstraintSolver solver = new SequentialImpulseConstraintSolver();
            DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

            WorldPhysics worldPhysics = new BulletWorldPhysics(world, dynamicsWorld);
            worlds.add(worldPhysics);

            return worldPhysics;
        }
    }

    /**
     * Unregister a world from the physics engine
     * @param world The world to unregister
     */
    public void unregisterWorld(World world) {
        synchronized (lock) {
            WorldPhysics worldPhysics = getWorldPhysics(world.getUID());
            if (worldPhysics != null) {
                worldPhysics.clear();
                worlds.remove(worldPhysics);
            }
        }
    }

    /**
     * Start the physics simulation for a world if it's not already running
     * @param worldPhysics The WorldPhysics instance to start
     */
    private void startWorldPhysics(WorldPhysics worldPhysics) {
        synchronized (lock) {
            UUID worldId = worldPhysics.getUniqueId();
            if (threadManager.isTaskRunning(worldId)) {
                return;
            }

            threadManager.submitTask(worldId, () -> runWorldPhysics(worldPhysics));
        }
    }

    /**
     * Stop the physics simulation for a world
     * @param worldPhysics The WorldPhysics instance to stop
     */
    private void stopWorldPhysics(WorldPhysics worldPhysics) {
        synchronized (lock) {
            UUID worldId = worldPhysics.getUniqueId();
            if (!threadManager.isTaskRunning(worldId)) {
                return;
            }
            threadManager.cancelTask(worldId);
        }
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
    public RigidBlock createBox(Location location, BlockData data, float mass, float xScale, float yScale, float zScale) {
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
    public WorldPhysics getWorldPhysics(UUID uniqueId) {
        synchronized (lock) {
            for (WorldPhysics world : worlds) {
                if (world.getUniqueId().equals(uniqueId)) {
                    return world;
                }
            }
        }
        return null;
    }

    /**
     * Clear all the physics objects
     */
    public void clear() {
        synchronized (lock){
            for (WorldPhysics world : worlds) {
                world.clear();
            }
        }
    }

    /**
     * Stop the physics engine
     */
    public void stop() {
        running = false;
        threadManager.shutdown();
    }

}
