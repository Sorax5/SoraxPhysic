package fr.phylisiumstudio.logic;

import fr.phylisiumstudio.logic.runnable.PhysicsThreadRunnable;

import java.util.UUID;
import java.util.concurrent.*;

public class PhysicsThreadManager {
    private final ScheduledExecutorService executorService;
    private final ConcurrentLinkedQueue<PhysicsThreadRunnable> physicsTasks;
    private final ConcurrentLinkedQueue<Future<?>> futures;

    public PhysicsThreadManager(int maxThreads) {
        this.executorService = Executors.newScheduledThreadPool(maxThreads);
        this.physicsTasks = new ConcurrentLinkedQueue<>();
        this.futures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < maxThreads; i++) {
            PhysicsThreadRunnable physicsThreadRunnable = new PhysicsThreadRunnable();
            Future<?> future = executorService.scheduleAtFixedRate(physicsThreadRunnable, 0, 50, TimeUnit.MILLISECONDS);
            physicsTasks.add(physicsThreadRunnable);
            futures.add(future);
        }
    }

    /**
     * Submit a task to the executor service
     * @param worldPhysics The world physics
     */
    public void registerWorld(WorldPhysics worldPhysics) {
        PhysicsThreadRunnable physicsThreadRunnable = getLeastBusyThread();
        physicsThreadRunnable.addWorld(worldPhysics);
    }

    /**
     * Cancel the task associated with the world
     * @param worldId The world id
     */
    public void unregisterWorld(UUID worldId) {
        PhysicsThreadRunnable physicsThreadRunnable = getPhysicsTask(worldId);
        if (physicsThreadRunnable != null) {
            WorldPhysics worldPhysics = physicsThreadRunnable.getWorld(worldId);
            physicsThreadRunnable.removeWorld(worldPhysics);
        }
    }

    /**
     * Arrêter le gestionnaire de threads
     */
    public void shutdown() {
        for (Future<?> future : this.futures) {
            future.cancel(true);
        }
        this.futures.clear();

        this.physicsTasks.clear();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private PhysicsThreadRunnable getLeastBusyThread() {
        PhysicsThreadRunnable leastBusy = null;
        int minTasks = Integer.MAX_VALUE;
        for (PhysicsThreadRunnable physicsThreadRunnable : physicsTasks) {
            int tasks = physicsThreadRunnable.getWorldsSize();
            if (tasks < minTasks) {
                minTasks = tasks;
                leastBusy = physicsThreadRunnable;
            }
        }
        return leastBusy;
    }

    private PhysicsThreadRunnable getPhysicsTask(UUID worldId) {
        return physicsTasks.stream()
                .filter(physicsThread -> physicsThread.getWorlds().stream().anyMatch(world -> world.getUniqueId().equals(worldId)))
                .findFirst()
                .orElse(null);
    }
}
