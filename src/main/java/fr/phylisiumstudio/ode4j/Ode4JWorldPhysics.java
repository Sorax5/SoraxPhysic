package fr.phylisiumstudio.ode4j;

import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.bullet.BulletRigidBlock;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.ode4j.ode.*;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Ode4JWorldPhysics extends WorldPhysics<DRigidBlock> {

    private final DWorld world;
    private final DSpace space;
    private float timespan;
    private int maxSubSteps;
    private boolean timeFreeze;
    private final List<DRigidBlock> blocks;

    private final World bukkitWorld;

    public Ode4JWorldPhysics(World bukkitWorld) {
        this.world = OdeHelper.createWorld();
        this.space = OdeHelper.createSimpleSpace();
        this.blocks = new ArrayList<>();
        this.timespan = 1.0f / 20;
        this.maxSubSteps = 10;
        this.timeFreeze = false;
        this.bukkitWorld = bukkitWorld;
    }

    @Override
    public void stepSimulation() {
        if (!timeFreeze) {
            world.step(timespan);

            for (DRigidBlock block : this.blocks) {

            }
        }
    }

    @Override
    public UUID getUniqueId() {
        return this.bukkitWorld.getUID();
    }

    @Override
    public String getWorldName() {
        return this.bukkitWorld.getName();
    }

    @Override
    public List<DRigidBlock> getBlocks() {
        return new ArrayList<>(blocks);
    }

    @Override
    public DRigidBlock createBox(Location location, BlockData blockData, float mass, float xScale, float yScale, float zScale) {
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

        DBody body = OdeHelper.createBody(world);
        DMass dMass = OdeHelper.createMass();
        dMass.setBoxTotal(mass, xScale, yScale, zScale);
        body.setMass(dMass);
        body.setPosition(location.getX(), location.getY(), location.getZ());
        DRigidBlock rigidBlock = new DRigidBlock(body, blockDisplay, interaction);
        blocks.add(rigidBlock);
        return rigidBlock;
    }

    @Override
    public DRigidBlock createSphere(Location location, BlockData blockData, float radius, float mass) {
        assert location.getWorld().equals(bukkitWorld);
        float length = (float) (radius * Math.sqrt(2));

        BlockDisplay blockDisplay = bukkitWorld.spawn(location, BlockDisplay.class, display -> {
            display.setBlock(blockData);
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

        DBody body = OdeHelper.createBody(world);
        DMass dMass = OdeHelper.createMass();
        dMass.setSphereTotal(mass, radius);
        body.setMass(dMass);
        body.setPosition(location.getX(), location.getY(), location.getZ());

        DRigidBlock rigidBlock = new DRigidBlock(body, blockDisplay, hitbox);
        blocks.add(rigidBlock);
        return rigidBlock;
    }

    @Override
    public void removeBlock(DRigidBlock block) {
        synchronized (PhysicsManager.lock){
            blocks.remove(block);
            block.getRigidBody().destroy();
            block.getBlockDisplay().remove();
            block.getInteraction().remove();
        }
    }

    @Override
    public void clear() {
        for (DRigidBlock block : this.blocks) {
            removeBlock(block);
        }
        synchronized (PhysicsManager.lock){
            this.blocks.clear();
        }
    }

    @Override
    public DRigidBlock getBlock(UUID id) {
        return blocks.stream().filter(block -> block.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void convertChunk(Vector3f pos1, Vector3f pos2) {
        // Implement chunk conversion logic
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
        this.timeFreeze = freeze;
    }

    @Override
    public boolean isFrozen() {
        return timeFreeze;
    }

    @Override
    public boolean isRunning() {
        // Implement logic to check if the world can run
        return true;
    }

    @Override
    public void linkRigidBlock(DRigidBlock block1, DRigidBlock block2) {
        // Implement linking logic
    }
}