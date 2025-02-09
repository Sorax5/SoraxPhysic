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

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    @Override
    public List<RigidBlock> getBlocks() {
        return blocks;
    }

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

    @Override
    public void removeBlock(RigidBlock block) {
        scene.removeActor(((PhysxRigidBlock) block).getRigidDynamic());
        block.getBlockDisplay().remove();
        block.getInteraction().remove();
        blocks.remove(block);
    }

    @Override
    public void clear() {
        for (RigidBlock block : blocks) {
            scene.removeActor(((PhysxRigidBlock) block).getRigidDynamic());
            block.getBlockDisplay().remove();
            block.getInteraction().remove();
        }
        blocks.clear();
    }

    @Override
    public RigidBlock getBlock(UUID id) {
        return blocks.stream().filter(block -> block.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void convertChunk(Vector3f pos1, Vector3f pos2) {
        int startX = (int) Math.min(pos1.x, pos2.x);
        int startY = (int) Math.min(pos1.y, pos2.y);
        int startZ = (int) Math.min(pos1.z, pos2.z);

        int endX = (int) Math.max(pos1.x, pos2.x);
        int endY = (int) Math.max(pos1.y, pos2.y);
        int endZ = (int) Math.max(pos1.z, pos2.z);

        // Calculate the center and half-sizes of the bounding box
        float centerX = (startX + endX) / 2.0f;
        float centerY = (startY + endY) / 2.0f;
        float centerZ = (startZ + endZ) / 2.0f;

        float halfSizeX = (endX - startX) / 2.0f;
        float halfSizeY = (endY - startY) / 2.0f;
        float halfSizeZ = (endZ - startZ) / 2.0f;

        // Create the custom shape for the chunk
        PxVec3 position = new PxVec3(centerX, centerY, centerZ);
        PxTransform transform = new PxTransform(position);
        PxBoxGeometry chunkGeometry = new PxBoxGeometry(halfSizeX, halfSizeY, halfSizeZ);
        PxMaterial material = physics.createMaterial(0.5f, 0.5f, 0.5f);
        PxShapeFlags shapeFlags = new PxShapeFlags((byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE.value | PxShapeFlagEnum.eSIMULATION_SHAPE.value));
        PxShape chunkShape = physics.createShape(chunkGeometry, material, true, shapeFlags);
        PxRigidStatic chunk = physics.createRigidStatic(transform);
        chunk.attachShape(chunkShape);
        scene.addActor(chunk);

        // Clean up temporary objects
        chunkGeometry.destroy();
        material.destroy();
        shapeFlags.destroy();
        position.destroy();
        transform.destroy();
    }

    @Override
    public float getTimespan() {
        return timespan;
    }

    @Override
    public void setTimespan(float timespan) {
        this.timespan = timespan;
    }

    @Override
    public int getMaxSubSteps() {
        return maxSubSteps;
    }

    @Override
    public void setMaxSubSteps(int maxSubSteps) {
        this.maxSubSteps = maxSubSteps;
    }

    @Override
    public void setFreeze(boolean freeze) {
        isFrozen = freeze;
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    @Override
    public boolean isRunning() {
        return !isFrozen;
    }
}