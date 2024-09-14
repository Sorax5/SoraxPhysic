package fr.phylisiumstudio.logic;

import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public abstract class IPhysicsManager<T extends WorldPhysics, W extends IRigidBlock> {

    private final WorldManager worldManager;
    private final Logger logger = LoggerFactory.getLogger(PhysicsManager.class);
    public static final Object lock = new Object();


    /**
     * Create a new physics manager
     */
    public IPhysicsManager() {
        this.worldManager = new WorldManager();
    }

    /**
     * Register a world with the physics engine
     * @param world The world to register
     */
    public void registerWorld(T world){
        this.worldManager.registerWorld(world);
    }

    /**
     * Unregister a world from the physics engine
     * @param world The world to unregister
     */
    public void unregisterWorld(T world){
        this.worldManager.unregisterWorld(world);
    }

    W createBox(Location location, BlockData data, float mass, float xScale, float yScale, float zScale);

    void createSphere(Location location, BlockData data, float mass, float radius);

    public T getWorldPhysics(UUID uniqueId){
        return this.worldManager.getWorld(uniqueId);
    }

    void clear();

    void stop();

    void pauseAllWorlds();

    void resumeAllWorlds();

    public List<T> getAllWorlds(){
        return this.worldManager.getWorlds();
    }

    boolean isWorldSimulated(UUID worldId);
}
