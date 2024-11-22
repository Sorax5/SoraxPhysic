package fr.phylisiumstudio.physx.models;

import fr.phylisiumstudio.soraxPhysic.SoraxPhysic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxRigidDynamic;

public class AdaptBukkitTransform {
    public void adaptBukkitToPhysx(PhysxRigidBlock block) {
        PxRigidDynamic rigidDynamic = block.getRigidDynamic();
        PxTransform pxTransform = rigidDynamic.getGlobalPose();
        PxVec3 pxPosition = pxTransform.getP();
        physx.common.PxQuat pxQuat = pxTransform.getQ();

        runOnMainThread(() -> updateEntities(block, pxPosition, pxQuat));
    }

    private void updateEntities(PhysxRigidBlock block, PxVec3 position, physx.common.PxQuat quaternion) {
        BlockDisplay blockDisplay = block.getBlockDisplay();
        Interaction interaction = block.getInteraction();

        Location displayLocation = blockDisplay.getLocation();
        Transformation displayTransformation = blockDisplay.getTransformation();
        Transformation newTransformation = getTransformation(quaternion, displayTransformation);
        blockDisplay.setTransformation(newTransformation);

        displayLocation.setX(position.getX());
        displayLocation.setY(position.getY());
        displayLocation.setZ(position.getZ());
        blockDisplay.teleport(displayLocation);

        displayLocation.setY(position.getY() - (displayTransformation.getScale().y / 2));
        interaction.teleport(displayLocation);
    }

    @NotNull
    private static Transformation getTransformation(physx.common.PxQuat pxQuat, Transformation displayTransformation) {
        Quaternionf quaternionf = new Quaternionf(pxQuat.getX(), pxQuat.getY(), pxQuat.getZ(), pxQuat.getW());

        Vector3f scale = displayTransformation.getScale();
        Vector3f centreOffset = new Vector3f(-scale.x / 2, -scale.y / 2, -scale.z / 2);

        Matrix4f rotationMatrix = new Matrix4f().rotation(quaternionf);
        Vector3f rotatedOffset = new Vector3f();
        rotationMatrix.transformPosition(centreOffset, rotatedOffset);

        return new Transformation(rotatedOffset, displayTransformation.getLeftRotation(), displayTransformation.getScale(), quaternionf);
    }

    public void runOnMainThread(Runnable runnable) {
        Bukkit.getScheduler().runTask(SoraxPhysic.getInstance(), runnable);
    }
}
