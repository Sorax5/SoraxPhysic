package fr.phylisiumstudio.bullet;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import fr.phylisiumstudio.logic.IPhysicsEngineFactory;
import fr.phylisiumstudio.logic.IWorldPhysics;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class WorldPhysics implements IWorldPhysics {
    private final DiscreteDynamicsWorld dynamicsWorld;
    private final List<RigidBlock> rigidBlocks = new ArrayList<>();
    private final IPhysicsEngineFactory physicsFactory;
    private final Object lock = new Object();

    public WorldPhysics(IPhysicsEngineFactory physicsFactory) {
        this.physicsFactory = physicsFactory;
        this.dynamicsWorld = physicsFactory.createDynamicsWorld();
    }

    @Override
    public void stepSimulation() {
        synchronized (lock) {
            dynamicsWorld.stepSimulation(1/20f, 30);
        }
    }

    @Override
    public RigidBody createBoxShape(Location location, Material material, float mass, float xScale, float yScale, float zScale) {
        // Abstraction de la création des objets physiques
        Vector3f dimensions = new Vector3f(xScale / 2, yScale / 2, zScale / 2);
        BoxShape boxShape = physicsFactory.createBoxShape(dimensions);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set((float) location.getX(), (float) location.getY(), (float) location.getZ());

        RigidBody body = physicsFactory.createRigidBody(mass, boxShape, transform);
        dynamicsWorld.addRigidBody(body);

        // Gestion de l'affichage du bloc dans Bukkit
        createDisplayAndHitbox(location, material, xScale, yScale, zScale, body);

        return body;
    }

    @Override
    public void createSphereShape(Location location, Material material, float mass, float radius) {
        // Création d'une sphère
        SphereShape sphereShape = physicsFactory.createSphereShape(radius);

        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set((float) location.getX(), (float) location.getY(), (float) location.getZ());

        RigidBody body = physicsFactory.createRigidBody(mass, sphereShape, transform);
        dynamicsWorld.addRigidBody(body);

        createDisplayAndHitbox(location, material, radius, radius, radius, body);
    }

    private void createDisplayAndHitbox(Location location, Material material, float xScale, float yScale, float zScale, RigidBody body) {
        BlockDisplay blockDisplay = location.getWorld().spawn(location, BlockDisplay.class, display -> {
            display.setTeleportDuration(1);
            display.setBlock(material.createBlockData());
            Transformation transformation = display.getTransformation();
            org.joml.Vector3f translation = new org.joml.Vector3f(-xScale / 2, -yScale / 2, -zScale / 2);
            org.joml.Vector3f scale = new org.joml.Vector3f(xScale, yScale, zScale);
            display.setTransformation(new Transformation(translation, transformation.getLeftRotation(), scale, transformation.getRightRotation()));
        });

        Interaction hitbox = location.getWorld().spawn(location, Interaction.class, interaction -> {
            interaction.setInteractionHeight(yScale);
            interaction.setInteractionWidth(zScale);
            interaction.setResponsive(true);
        });

        RigidBlock rigidBlock = new RigidBlock(body, blockDisplay, hitbox);
        rigidBlocks.add(rigidBlock);
    }

    @Override
    public void clearAll() {
        synchronized (lock) {
            for (RigidBlock rigidBlock : rigidBlocks) {
                dynamicsWorld.removeRigidBody(rigidBlock.getRigidBody());
                rigidBlock.getBlockDisplay().remove();
                rigidBlock.getInteraction().remove();
            }
            rigidBlocks.clear();
        }
    }
}
