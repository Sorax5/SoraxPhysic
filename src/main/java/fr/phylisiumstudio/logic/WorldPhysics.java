package fr.phylisiumstudio.logic;

import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Interface for the physics world
 */
public abstract class WorldPhysics {

    private final Lock lock = new ReentrantLock();

    /**
     * Step the simulation
     */
    public abstract void stepSimulation();

    /**
     * Get the unique id of the world
     *
     * @return the unique id
     */
    public abstract UUID getUniqueId();

    /**
     * Get the blocks
     * @return the blocks
     */
    public abstract List<RigidBlock> getBlocks();

    /**
     * Create a box
     *
     * @param location  the location (must be in the same world)
     * @param blockData the block data to use
     * @param mass      the mass of the block
     * @param xScale    the x scale
     * @param yScale    the y scale
     * @param zScale    the z scale
     * @return the box
     */
    public abstract RigidBlock createBox(Location location, BlockData blockData, float mass, float xScale, float yScale, float zScale);

    /**
     * Create a sphere
     *
     * @param location the location (must be in the same world)
     * @param radius   the radius
     * @param mass     the mass
     * @return the sphere
     */
    public abstract RigidBlock createSphere(Location location, BlockData data, float radius, float mass);

    /**
     * Remove a block
     * @param block the block to remove
     */
    public abstract void removeBlock(RigidBlock block);

    /**
     * Clear the world
     */
    public abstract void clear();

    /**
     * Get the block with the given id
     * @param id the id
     * @return the block
     */
    public abstract RigidBlock getBlock(UUID id);

    /**
     * convert region physics
     * @param pos1 the first position
     * @param pos2 the second position
     */
    public abstract void convertChunk(Vector3f pos1, Vector3f pos2);

    /**
     * Get the lock
     */
    public Lock getLock() {
        return lock;
    }

    /**
     * Get the time freeze
     */
    public abstract float getTimespan();

    /**
     * Set the time freeze
     */
    public abstract void setTimespan(float timeFreeze);

    /**
     * Get Max substeps
     */
    public abstract int getMaxSubSteps();

    /**
     * Set Max substeps
     */
    public abstract void setMaxSubSteps(int maxSubSteps);
}
