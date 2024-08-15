package fr.phylisiumstudio.soraxPhysic.models;

import com.bulletphysics.dynamics.RigidBody;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.joml.Vector3f;

import java.util.UUID;

public class RigidBlock {
    private final UUID uniqueId;
    private RigidBody rigidBody;
    private BlockDisplay blockDisplay;
    private Interaction interaction;

    public RigidBlock(RigidBody rigidBody, BlockDisplay blockDisplay, Interaction interaction) {
        this.rigidBody = rigidBody;
        this.blockDisplay = blockDisplay;
        this.interaction = interaction;
        this.uniqueId = UUID.randomUUID();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public RigidBody getRigidBody() {
        return rigidBody;
    }

    public BlockDisplay getBlockDisplay() {
        return blockDisplay;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public void applyImpulse(Vector3f direction, Vector3f impulse){
        if (!rigidBody.isActive()){
            rigidBody.activate();
        }
        javax.vecmath.Vector3f impulseVec = new javax.vecmath.Vector3f(impulse.x, impulse.y, impulse.z);
        javax.vecmath.Vector3f directionVec = new javax.vecmath.Vector3f(direction.x, direction.y, direction.z);

        rigidBody.applyImpulse(impulseVec, directionVec);
        rigidBody.updateInertiaTensor();
    }
}
