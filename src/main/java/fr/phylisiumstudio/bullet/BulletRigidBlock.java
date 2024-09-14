package fr.phylisiumstudio.bullet;

import com.bulletphysics.dynamics.RigidBody;
import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.joml.Vector3f;

import java.util.UUID;

public class BulletRigidBlock implements IRigidBlock<RigidBody> {
    private final UUID uniqueId;
    private final RigidBody rigidBody;
    private final BlockDisplay blockDisplay;
    private final Interaction interaction;

    public BulletRigidBlock(RigidBody rigidBody, BlockDisplay blockDisplay, Interaction interaction) {
        this.rigidBody = rigidBody;
        this.blockDisplay = blockDisplay;
        this.interaction = interaction;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public RigidBody getRigidBody() {
        return rigidBody;
    }

    @Override
    public BlockDisplay getBlockDisplay() {
        return blockDisplay;
    }

    @Override
    public Interaction getInteraction() {
        return interaction;
    }

    @Override
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
