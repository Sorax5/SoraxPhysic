package fr.phylisiumstudio.logic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ThreadManager {
    private final ExecutorService executorService;
    private final Map<UUID, Future<?>> worldTasks;

    public ThreadManager(int maxThreads) {
        this.executorService = new ThreadPoolExecutor(
                maxThreads,
                maxThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );

        this.worldTasks = new ConcurrentHashMap<>();
    }

    /**
     * Soumettre une tâche pour un monde spécifique
     *
     * @param worldId L'identifiant unique du monde
     * @param task La tâche à exécuter
     */
    public void submitTask(UUID worldId, Runnable task) {
        Future<?> future = executorService.submit(task);
        worldTasks.put(worldId, future);
    }

    /**
     * Annuler la tâche associée à un monde spécifique
     *
     * @param worldId L'identifiant unique du monde
     */
    public void cancelTask(UUID worldId) {
        Future<?> future = worldTasks.remove(worldId);
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Vérifier si une tâche est en cours d'exécution pour un monde spécifique
     *
     * @param worldId L'identifiant unique du monde
     * @return Vrai si une tâche est en cours, sinon faux
     */
    public boolean isTaskRunning(UUID worldId) {
        Future<?> future = worldTasks.get(worldId);
        return future != null && !future.isDone();
    }

    /**
     * Arrêter le gestionnaire de threads
     */
    public void shutdown() {
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
}
