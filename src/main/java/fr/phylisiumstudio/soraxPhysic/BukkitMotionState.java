package fr.phylisiumstudio.soraxPhysic;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.joml.Matrix4f;

import javax.vecmath.Quat4f;

public class BukkitMotionState extends MotionState {
    private final Transform transform = new Transform();

    private final RigidBlock rigidBlock;

    public BukkitMotionState(RigidBlock rigidBlock) {
        this.rigidBlock = rigidBlock;
    }

    @Override
    public Transform getWorldTransform(Transform out) {
        transform.set(out);
        return out;
    }

    @Override
    public void setWorldTransform(Transform worldTrans) {
        transform.set(worldTrans);
        Quat4f quarternion = new Quat4f();
        transform.getRotation(quarternion);

        runOnMainThread(() -> updateEntities(transform, quarternion));
    }

    private void updateEntities(Transform transform, Quat4f quaternion) {
        BlockDisplay blockDisplay = rigidBlock.getBlockDisplay();
        Interaction interaction = rigidBlock.getInteraction();

        Location displayLocation = blockDisplay.getLocation();
        Transformation displayTransformation = blockDisplay.getTransformation();
        Transformation newTransformation = getTransformation(quaternion, displayTransformation);
        blockDisplay.setTransformation(newTransformation);

        displayLocation.setX(transform.origin.x);
        displayLocation.setY(transform.origin.y);
        displayLocation.setZ(transform.origin.z);
        blockDisplay.teleport(displayLocation);

        displayLocation.setY(transform.origin.y - (displayTransformation.getScale().y/2));
        interaction.teleport(displayLocation);
    }

    @NotNull
    private static Transformation getTransformation(Quat4f quarternion, Transformation displayTransformation) {
        Quaternionf quaternionf = new Quaternionf(quarternion.x, quarternion.y, quarternion.z, quarternion.w);

        Vector3f scale = displayTransformation.getScale();
        Vector3f centreOffset = new Vector3f(-scale.x/2, -scale.y/2, -scale.z/2);

        Matrix4f rotationMatrix = new Matrix4f().rotation(quaternionf);
        Vector3f rotatedOffset = new Vector3f();
        rotationMatrix.transformPosition(centreOffset, rotatedOffset);

        return new Transformation(rotatedOffset, displayTransformation.getLeftRotation(), displayTransformation.getScale(), quaternionf);
    }

    public void runOnMainThread(Runnable runnable) {
        Bukkit.getScheduler().runTask(SoraxPhysic.getInstance(), runnable);
    }

    public RigidBlock getRigidBlock() {
        return rigidBlock;
    }
}
