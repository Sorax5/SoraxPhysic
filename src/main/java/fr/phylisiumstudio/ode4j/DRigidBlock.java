package fr.phylisiumstudio.ode4j;

import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.joml.Vector3f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;

import java.util.UUID;

public class DRigidBlock implements IRigidBlock<DBody> {

    private final UUID uniqueId;
    private final DBody rigidBody;
    private final BlockDisplay blockDisplay;
    private final Interaction interaction;

    public DRigidBlock(DBody rigidBody, BlockDisplay blockDisplay, Interaction interaction) {
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
    public DBody getRigidBody() {
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
    public void applyImpulse(Vector3f direction, Vector3f impulse) {
        if (!rigidBody.isEnabled()) {
            rigidBody.enable();
        }
        javax.vecmath.Vector3f impulseVec = new javax.vecmath.Vector3f(impulse.x, impulse.y, impulse.z);
        javax.vecmath.Vector3f directionVec = new javax.vecmath.Vector3f(direction.x, direction.y, direction.z);

        DVector3C dImpulseVec = new DVector3(impulse.x, impulse.y, impulse.z);
        DVector3C dDirectionVec = new DVector3(direction.x, direction.y, direction.z);

        rigidBody.addForce(dImpulseVec);
        rigidBody.addTorque(dDirectionVec);
    }
}
