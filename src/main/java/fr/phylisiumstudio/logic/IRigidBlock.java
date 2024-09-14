package fr.phylisiumstudio.logic;

import com.bulletphysics.dynamics.RigidBody;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.joml.Vector3f;

import java.util.UUID;

public interface IRigidBlock<T> {
    UUID getUniqueId();

    T getRigidBody();

    BlockDisplay getBlockDisplay();

    Interaction getInteraction();

    void applyImpulse(Vector3f direction, Vector3f impulse);
}
