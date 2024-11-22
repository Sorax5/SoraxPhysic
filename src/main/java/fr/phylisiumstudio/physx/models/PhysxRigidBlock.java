package fr.phylisiumstudio.physx.models;

import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.joml.Vector3f;
import physx.common.PxVec3;
import physx.physics.PxForceModeEnum;
import physx.physics.PxRigidDynamic;

public class PhysxRigidBlock extends RigidBlock {
    private final PxRigidDynamic rigidDynamic;

    public PhysxRigidBlock(BlockDisplay blockDisplay, Interaction interaction, PxRigidDynamic rigidDynamic) {
        super(blockDisplay, interaction);
        this.rigidDynamic = rigidDynamic;
    }

    @Override
    public void applyImpulse(Vector3f direction, Vector3f impulse) {
        PxVec3 pxDirection = new PxVec3(direction.x, direction.y, direction.z);
        PxVec3 pxImpulse = new PxVec3(impulse.x, impulse.y, impulse.z);
        rigidDynamic.addForce(pxImpulse, PxForceModeEnum.eIMPULSE);
    }

    public PxRigidDynamic getRigidDynamic() {
        return rigidDynamic;
    }
}
