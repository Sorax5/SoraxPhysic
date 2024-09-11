package fr.phylisiumstudio.logic.runnable;

import fr.phylisiumstudio.logic.WorldPhysics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runnable task for physics simulation
 */
public class PhysicsThreadRunnable implements Runnable {
    private final UUID id = UUID.randomUUID();
    private final List<WorldPhysics> worldPhysics;
    private final Logger logger = LoggerFactory.getLogger(PhysicsThreadRunnable.class);

    /**
     * Create a new physics task
     */
    public PhysicsThreadRunnable() {
        this.worldPhysics = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            Instant start = Instant.now();
            for (WorldPhysics worldPhysic : this.worldPhysics) {
                worldPhysic.stepSimulation();
            }
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            if (duration.toMillis() > 50) {
                logger.warn("PhysicsTask #" + id + " is running slow: " + duration.toMillis() + "ms");
            }
        } catch (NullPointerException e) {
            logger.error("NullPointerException in world physics thread #" + id, e);
        } catch (Exception e) {
            logger.error("Error in world physics thread #" + id, e);
        }
    }

    /**
     * Add a world to the physics task
     * @param worldPhysics The world to add
     */
    public void addWorld(WorldPhysics worldPhysics) {
        this.worldPhysics.add(worldPhysics);
    }

    /**
     * Remove a world from the physics task
     * @param worldPhysics The world to remove
     */
    public void removeWorld(WorldPhysics worldPhysics) {
        this.worldPhysics.remove(worldPhysics);
    }

    public int getWorldsSize() {
        return this.worldPhysics.size();
    }

    public List<WorldPhysics> getWorlds() {
        return new ArrayList<>(this.worldPhysics);
    }

    public WorldPhysics getWorld(UUID worldId) {
        return this.worldPhysics.stream()
                .filter(world -> world.getUniqueId().equals(worldId))
                .findFirst()
                .orElse(null);
    }
}
