package fr.phylisiumstudio.bullet;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.concurrent.*;

/**
 * A world physics implementation using bullet physics
 */
public class BulletWorldPhysics extends WorldPhysics {

    private DynamicsWorld bulletWorld;
    private final World bukkitWorld;

    private final List<RigidBlock> blocks;

    private float timespan = 1.0f / 20.0f;
    private int maxSubSteps = 30;
    private boolean timeFreeze = false;

    private final Logger logger = LoggerFactory.getLogger(BulletWorldPhysics.class);

    public BulletWorldPhysics(World bukkitWorld) {
        this.bukkitWorld = bukkitWorld;
        this.blocks = new ArrayList<>();

        try {
            BroadphaseInterface broadphase = new DbvtBroadphase();
            CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
            CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
            ConstraintSolver solver = new SequentialImpulseConstraintSolver();

            this.bulletWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
            logger.info("BulletWorld for world " + bukkitWorld.getName() + " initialized");
        } catch (Exception e) {
            logger.error("Error initializing BulletWorld for world " + bukkitWorld.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Step the simulation
     */
    @Override
    public void stepSimulation() {
        if (!isRunning()){
            return;
        }

        if (bulletWorld == null) {
            logger.error("bulletWorld is null");
            return;
        }

        if (timeFreeze) return;
        synchronized (PhysicsManager.lock) {
            try{
                bulletWorld.stepSimulation(timespan, maxSubSteps);
            }
            catch (Exception e){
                logger.error("Error stepping simulation: " + e.getMessage(), e);
            }

        }
    }

    /**
     * Get the unique id of the world (bukkit world)
     *
     * @return the unique id
     */
    @Override
    public UUID getUniqueId() {
        return bukkitWorld.getUID();
    }

    /**
     * Get the world name
     *
     * @return the world name
     */
    @Override
    public String getWorldName() {
        return bukkitWorld.getName();
    }

    /**
     * Get the blocks
     *
     * @return the blocks
     */
    @Override
    public List<RigidBlock> getBlocks() {
        return this.blocks;
    }

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
    @Override
    public RigidBlock createBox(Location location, BlockData blockData, float mass, float xScale, float yScale, float zScale) {
        assert location.getWorld().equals(bukkitWorld);

        BlockDisplay blockDisplay = bukkitWorld.spawn(location, BlockDisplay.class, display -> {
            display.setBlock(blockData);
            display.setRotation(0, 0);
            display.setTeleportDuration(1);

            Transformation transformation = display.getTransformation();
            org.joml.Vector3f translation = new org.joml.Vector3f(-xScale/2, -yScale/2, -zScale/2);
            org.joml.Vector3f scale = new org.joml.Vector3f(xScale, yScale, zScale);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });

        Interaction interaction = bukkitWorld.spawn(location, Interaction.class, display -> {
            display.setInteractionHeight(xScale);
            display.setInteractionWidth(zScale);
            display.setResponsive(true);
        });

        Vector3f position = new Vector3f(xScale/2, yScale/2, zScale/2);
        BoxShape boxShape = new BoxShape(position);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set((float) location.getX(), (float) location.getY(), (float) location.getZ());

        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(3, localInertia);


        RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, null, boxShape, localInertia);
        RigidBody body = new RigidBody(constructionInfo);
        body.setWorldTransform(transform);
        body.setRestitution(0.0f);

        RigidBlock rigidBlock = new RigidBlock(body, blockDisplay, interaction);
        BukkitMotionState motionState = new BukkitMotionState(rigidBlock);
        body.setMotionState(motionState);

        synchronized (PhysicsManager.lock) {
            bulletWorld.addRigidBody(body);
            synchronized (blocks) {
                blocks.add(rigidBlock);
            }
        }

        Chunk chunk = location.getChunk();
        Vector3f pos1 = new Vector3f(chunk.getX() * 16, 0, chunk.getZ() * 16);
        Vector3f pos2 = new Vector3f(chunk.getX() * 16 + 16, 256, chunk.getZ() * 16 + 16);
        convertChunk(pos1, pos2);

        return rigidBlock;
    }

    /**
     * Create a sphere
     *
     * @param location the location (must be in the same world)
     * @param radius   the radius
     * @param mass     the mass
     * @return the sphere
     */
    @Override
    public RigidBlock createSphere(Location location, BlockData data, float radius, float mass) {
        assert location.getWorld().equals(bukkitWorld);
        float length = (float) (radius * Math.sqrt(2));

        BlockDisplay blockDisplay = bukkitWorld.spawn(location, BlockDisplay.class, display -> {
            display.setBlock(data);
            display.setRotation(0, 0);
            display.setInterpolationDuration(1);
            display.setTeleportDuration(1);

            Transformation transformation = display.getTransformation();
            org.joml.Vector3f translation = new org.joml.Vector3f(-length/2, -length/2, -length/2);
            org.joml.Vector3f scale = new org.joml.Vector3f(length, length, length);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });
        Interaction hitbox = bukkitWorld.spawn(location, Interaction.class, display -> {
            display.setInteractionHeight(length);
            display.setInteractionWidth(length);
            display.setResponsive(true);
        });
        SphereShape sphereShape = new SphereShape(radius);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set((float) location.getX(), (float) location.getY(), (float) location.getZ());

        Vector3f localInertia = new Vector3f(0, 0, 0);
        sphereShape.calculateLocalInertia(3, localInertia);

        RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, null, sphereShape, localInertia);
        RigidBody body = new RigidBody(constructionInfo);
        body.setWorldTransform(transform);
        body.setRestitution(0.0f);

        RigidBlock rigidBlock = new RigidBlock(body, blockDisplay, hitbox);
        BukkitMotionState motionState = new BukkitMotionState(rigidBlock);
        body.setMotionState(motionState);

        synchronized (PhysicsManager.lock) {
            bulletWorld.addRigidBody(body);
            synchronized (blocks) {
                blocks.add(rigidBlock);
            }
        }

        Chunk chunk = location.getChunk();
        Vector3f pos1 = new Vector3f(chunk.getX() * 16, 0, chunk.getZ() * 16);
        Vector3f pos2 = new Vector3f(chunk.getX() * 16 + 16, 256, chunk.getZ() * 16 + 16);
        convertChunk(pos1, pos2);

        return rigidBlock;
    }

    /**
     * Remove a block
     *
     * @param block the block to remove
     */
    @Override
    public void removeBlock(RigidBlock block) {
        bulletWorld.removeRigidBody(block.getRigidBody());
        block.getBlockDisplay().remove();
        block.getInteraction().remove();
        synchronized (blocks) {
            blocks.remove(block);
        }
    }

    /**
     * Clear the world
     */
    @Override
    public void clear() {
        synchronized (blocks) {
            for (RigidBlock block : blocks) {
                bulletWorld.removeRigidBody(block.getRigidBody());
                block.getBlockDisplay().remove();
                block.getInteraction().remove();
            }
            blocks.clear();
        }
    }


    /**
     * Get the block with the given id
     *
     * @param id the id
     * @return the block
     */
    @Override
    @Nullable
    public RigidBlock getBlock(UUID id) {
        return this.blocks.stream().filter(block -> block.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void convertChunk(Vector3f pos1, Vector3f pos2) {
        CompoundShape compoundShape = new CompoundShape();
        final int numThreads = Runtime.getRuntime().availableProcessors();

        int startX = (int) Math.min(pos1.x, pos2.x);
        int startZ = (int) Math.min(pos1.z, pos2.z);
        int startY = (int) Math.min(pos1.y, pos2.y);

        int endX = (int) Math.max(pos1.x, pos2.x);
        int endZ = (int) Math.max(pos1.z, pos2.z);
        int endY = (int) Math.max(pos1.y, pos2.y);

        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            List<Future<Void>> futures = new ArrayList<>();

            // Divide the work into smaller tasks
            for (int x = startX; x < endX; x++) {
                final int currentX = x;
                Future<Void> future = executor.submit(() -> {
                    for (int z = startZ; z < endZ; z++) {
                        for (int y = startY; y < endY; y++) {
                            Block block = bukkitWorld.getBlockAt(currentX, y, z);
                            if (block.getType().isAir() || !isAdjacentToAir(block)) {
                                continue;
                            }
                            BoxShape blockShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
                            Transform transformShape = new Transform();
                            transformShape.setIdentity();
                            transformShape.origin.set(new Vector3f(currentX + 0.5f, y + 0.5f, z + 0.5f));

                            synchronized (compoundShape) {
                                compoundShape.addChildShape(transformShape, blockShape);
                            }
                        }
                    }
                    return null;
                });
                futures.add(future);
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Create and add the CompoundShape to the dynamics world
            if (compoundShape.getNumChildShapes() > 0) {
                RigidBody rigidBody = createStaticRigidBody(compoundShape);
                bulletWorld.addRigidBody(rigidBody);
            }
        } catch (Exception e) {
            logger.error("Error creating executor service: " + e.getMessage(), e);
        }
    }

    private RigidBody createStaticRigidBody(CompoundShape compoundShape) {
        float mass = 0.0f;
        Vector3f inertia = new Vector3f(0, 0, 0);
        RigidBodyConstructionInfo chunkInfo = new RigidBodyConstructionInfo(mass, null, compoundShape, inertia);
        RigidBody groundBlock = new RigidBody(chunkInfo);
        Transform ntransform = new Transform();
        ntransform.setIdentity();
        groundBlock.setWorldTransform(ntransform);
        return groundBlock;
    }

    /**
     * Get the time freeze
     */
    @Override
    public float getTimespan() {
        return timespan;
    }

    /**
     * Set the time freeze
     *
     * @param timespan
     */
    @Override
    public void setTimespan(float timespan) {
        this.timespan = timespan;
    }

    /**
     * Get Max substeps
     */
    @Override
    public int getMaxSubSteps() {
        return maxSubSteps;
    }

    /**
     * Set Max substeps
     *
     * @param maxSubSteps
     */
    @Override
    public void setMaxSubSteps(int maxSubSteps) {
        this.maxSubSteps = maxSubSteps;
    }

    /**
     * set freeze
     *
     * @param freeze the freeze
     */
    @Override
    public void setFreeze(boolean freeze) {
        this.timeFreeze = freeze;
    }

    /**
     * is frozen
     *
     * @return is frozen
     */
    @Override
    public boolean isFrozen() {
        return timeFreeze;
    }

    /**
     * Verify if the world can run
     *
     * @return if the world can run
     */
    @Override
    public boolean isRunning() {
        return !bukkitWorld.getPlayers().isEmpty();
    }

    private boolean isAdjacentToAir(Block block) {
        for (BlockFace face : BlockFace.values()) {
            if (block.getRelative(face).getType().isAir()) {
                return true;
            }
        }
        return false;
    }
}
