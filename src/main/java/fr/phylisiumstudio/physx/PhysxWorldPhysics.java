package fr.phylisiumstudio.physx;

import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.physx.models.AdaptBukkitTransform;
import fr.phylisiumstudio.physx.models.PhysxRigidBlock;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.geometry.PxBoxGeometry;
import physx.geometry.PxSphereGeometry;
import physx.physics.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhysxWorldPhysics extends WorldPhysics {
    private static PxFoundation foundation;
    private static PxPhysics physics;
    private static PxDefaultAllocator allocator;
    private static PxDefaultErrorCallback errorCb;

    private final UUID uniqueId;
    private final String worldName;
    private final PxScene scene;
    private final List<RigidBlock> blocks;
    private final World bukkitWorld;
    private boolean isFrozen;
    private float timespan;
    private int maxSubSteps;

    private AdaptBukkitTransform adaptBukkitTransform = new AdaptBukkitTransform();

    public PhysxWorldPhysics(UUID uniqueId, String worldName, World bukkitWorld) {
        this.uniqueId = uniqueId;
        this.worldName = worldName;
        this.blocks = new CopyOnWriteArrayList<>();
        this.isFrozen = false;
        this.timespan = 1.0f / 60.0f;
        this.maxSubSteps = 4;
        this.bukkitWorld = bukkitWorld;

        initializePhysX();

        int numThreads = 4;
        PxDefaultCpuDispatcher cpuDispatcher = PxTopLevelFunctions.DefaultCpuDispatcherCreate(numThreads);
        PxVec3 gravity = new PxVec3(0f, -9.81f, 0f);
        PxTolerancesScale tolerances = new PxTolerancesScale();
        PxSceneDesc sceneDesc = new PxSceneDesc(tolerances);
        sceneDesc.setGravity(gravity);
        sceneDesc.setCpuDispatcher(cpuDispatcher);
        sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
        this.scene = physics.createScene(sceneDesc);

        // Clean up temporary objects
        gravity.destroy();
        sceneDesc.destroy();
        tolerances.destroy();
    }

    private static synchronized void initializePhysX() {
        if (foundation == null) {
            int version = PxTopLevelFunctions.getPHYSICS_VERSION();
            allocator = new PxDefaultAllocator();
            errorCb = new PxDefaultErrorCallback();
            foundation = PxTopLevelFunctions.CreateFoundation(version, allocator, errorCb);
            PxTolerancesScale tolerances = new PxTolerancesScale();
            physics = PxTopLevelFunctions.CreatePhysics(version, foundation, tolerances);
            tolerances.destroy();
        }
    }


    /**
     * Step the simulation
     */
    @Override
    public void stepSimulation() {
        if (!isFrozen) {
            scene.simulate(timespan);
            scene.fetchResults(true);
            for (RigidBlock block : blocks) {
                adaptBukkitTransform.adaptBukkitToPhysx((PhysxRigidBlock) block);
            }
        }
    }

    /**
     * Get the unique id of the world
     *
     * @return the unique id
     */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the world name
     *
     * @return the world name
     */
    @Override
    public String getWorldName() {
        return worldName;
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
            org.joml.Vector3f translation = new org.joml.Vector3f(-xScale / 2, -yScale / 2, -zScale / 2);
            org.joml.Vector3f scale = new org.joml.Vector3f(xScale, yScale, zScale);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });

        Interaction interaction = bukkitWorld.spawn(location, Interaction.class, display -> {
            display.setInteractionHeight(xScale);
            display.setInteractionWidth(zScale);
            display.setResponsive(true);
        });

        PxVec3 position = new PxVec3((float) location.getX(), (float) location.getY(), (float) location.getZ());
        PxTransform transform = new PxTransform(position);
        PxRigidDynamic rigidDynamic = physics.createRigidDynamic(transform);

        PxBoxGeometry boxGeometry = new PxBoxGeometry(xScale / 2, yScale / 2, zScale / 2);
        PxMaterial material = physics.createMaterial(0.5f, 0.5f, 0.5f);
        PxShapeFlags shapeFlags = new PxShapeFlags((byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE.value | PxShapeFlagEnum.eSIMULATION_SHAPE.value));
        PxShape shape = physics.createShape(boxGeometry, material, true, shapeFlags);

        rigidDynamic.attachShape(shape);
        scene.addActor(rigidDynamic);

        RigidBlock block = new PhysxRigidBlock(blockDisplay, interaction, rigidDynamic);
        blocks.add(block);

        boxGeometry.destroy();
        material.destroy();
        shapeFlags.destroy();

        return block;
    }

    /**
     * Create a sphere
     *
     * @param location the location (must be in the same world)
     * @param data
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
            org.joml.Vector3f translation = new org.joml.Vector3f(-length / 2, -length / 2, -length / 2);
            org.joml.Vector3f scale = new org.joml.Vector3f(length, length, length);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });

        Interaction interaction = bukkitWorld.spawn(location, Interaction.class, display -> {
            display.setInteractionHeight(length);
            display.setInteractionWidth(length);
            display.setResponsive(true);
        });

        PxVec3 position = new PxVec3((float) location.getX(), (float) location.getY(), (float) location.getZ());
        PxTransform transform = new PxTransform(position);
        PxRigidDynamic rigidDynamic = physics.createRigidDynamic(transform);

        PxSphereGeometry sphereGeometry = new PxSphereGeometry(radius);
        PxMaterial material = physics.createMaterial(0.5f, 0.5f, 0.5f);
        PxShapeFlags shapeFlags = new PxShapeFlags((byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE.value | PxShapeFlagEnum.eSIMULATION_SHAPE.value));
        PxShape shape = physics.createShape(sphereGeometry, material, true, shapeFlags);

        rigidDynamic.attachShape(shape);
        scene.addActor(rigidDynamic);

        RigidBlock block = new PhysxRigidBlock(blockDisplay, interaction, rigidDynamic);
        blocks.add(block);

        sphereGeometry.destroy();
        material.destroy();
        shapeFlags.destroy();

        return block;
    }

    /**
     * Remove a block
     *
     * @param block the block to remove
     */
    @Override
    public void removeBlock(RigidBlock block) {
        scene.removeActor(((PhysxRigidBlock) block).getRigidDynamic());
        block.getBlockDisplay().remove();
        block.getInteraction().remove();
        blocks.remove(block);
    }

    /**
     * Clear the world
     */
    @Override
    public void clear() {
        for (RigidBlock block : blocks) {
            scene.removeActor(((PhysxRigidBlock) block).getRigidDynamic());
            block.getBlockDisplay().remove();
            block.getInteraction().remove();
        }
        blocks.clear();
    }

    /**
     * Get the block with the given id
     *
     * @param id the id
     * @return the block
     */
    @Override
    public RigidBlock getBlock(UUID id) {
        return blocks.stream().filter(block -> block.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    /**
     * convert region physics
     *
     * @param pos1 the first position
     * @param pos2 the second position
     */
    @Override
    public void convertChunk(Vector3f pos1, Vector3f pos2) {
        int startX = (int) Math.min(pos1.x, pos2.x);
        int startY = (int) Math.min(pos1.y, pos2.y);
        int startZ = (int) Math.min(pos1.z, pos2.z);

        int endX = (int) Math.max(pos1.x, pos2.x);
        int endY = (int) Math.max(pos1.y, pos2.y);
        int endZ = (int) Math.max(pos1.z, pos2.z);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = bukkitWorld.getBlockAt(x, y, z);
                    if (!block.getType().isAir()) {
                        Location location = block.getLocation();
                        BlockData blockData = block.getBlockData();
                        createBox(location, blockData, 0, 1, 1, 1);
                    }
                }
            }
        }
    }

    /**
     * Get the time span
     */
    @Override
    public float getTimespan() {
        return timespan;
    }

    /**
     * Set the time span
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
        isFrozen = freeze;
    }

    /**
     * is frozen
     *
     * @return is frozen
     */
    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    /**
     * Verify if the world can run
     *
     * @return if the world can run
     */
    @Override
    public boolean isRunning() {
        return !isFrozen;
    }
}
