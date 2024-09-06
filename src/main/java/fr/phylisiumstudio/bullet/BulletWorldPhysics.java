package fr.phylisiumstudio.bullet;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BulletWorldPhysics extends WorldPhysics {

    private final DynamicsWorld bulletWorld;
    private final World bukkitWorld;

    private final List<RigidBlock> blocks;

    private float timespan = 1.0f / 20.0f;
    private int maxSubSteps = 30;
    private boolean timeFreeze = false;

    public BulletWorldPhysics(World bukkitWorld, DiscreteDynamicsWorld bulletWorld) {
        this.bulletWorld = bulletWorld;
        this.bukkitWorld = bukkitWorld;
        this.blocks = new ArrayList<>();
    }

    /**
     * Step the simulation
     */
    @Override
    public void stepSimulation() {
        if (timeFreeze) return;
        bulletWorld.stepSimulation(timespan, maxSubSteps);
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
     * Get the blocks
     *
     * @return the blocks
     */
    @Override
    public List<RigidBlock> getBlocks() {
        return blocks;
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

        synchronized (PhysicsManager.lock){
            bulletWorld.addRigidBody(body);
            blocks.add(rigidBlock);
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

        synchronized (PhysicsManager.lock){
            bulletWorld.addRigidBody(body);
            blocks.add(rigidBlock);
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

    }

    /**
     * Clear the world
     */
    @Override
    public void clear() {
        synchronized (PhysicsManager.lock){
            for (RigidBlock block : blocks) {
                bulletWorld.removeRigidBody(block.getRigidBody());
                block.getBlockDisplay().remove();
                block.getInteraction().remove();
            }
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
        int startX = (int) Math.min(pos1.x, pos2.x);
        int endX = (int) Math.max(pos1.x, pos2.x);
        int startY = (int) Math.min(pos1.y, pos2.y);
        int endY = (int) Math.max(pos1.y, pos2.y);
        int startZ = (int) Math.min(pos1.z, pos2.z);
        int endZ = (int) Math.max(pos1.z, pos2.z);

        CompoundShape compoundShape = new CompoundShape();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    final int fx = x;
                    final int fy = y;
                    final int fz = z;
                    futures.add(executor.submit(() -> {
                        Block block = bukkitWorld.getBlockAt(fx, fy, fz);
                        if (block.getType().isAir()) return null;
                        Vector3f blockPos = new Vector3f(fx, fy, fz);

                        synchronized (compoundShape) {
                            addBlock(compoundShape, blockPos);
                        }
                        return null;
                    }));
                }
            }
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        Transform transform = new Transform();
        transform.setIdentity();

        float mass = 0.0f;
        Vector3f inertia = new Vector3f(0, 0, 0);

        DefaultMotionState motionState = new DefaultMotionState(transform);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motionState, compoundShape, inertia);
        RigidBody body = new RigidBody(rbInfo);

        bulletWorld.addRigidBody(body);
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


    private void addBlock(CompoundShape compoundShape, Vector3f blockPos) {
        Vector3f halfExtents = new Vector3f(0.5f, 0.5f, 0.5f);
        BoxShape boxShape = new BoxShape(halfExtents);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set(new Vector3f(blockPos.x + 0.5f, blockPos.y + 0.5f, blockPos.z + 0.5f));

        compoundShape.addChildShape(transform, boxShape);
    }
}
