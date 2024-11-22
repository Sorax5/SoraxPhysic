package fr.phylisiumstudio.soraxPhysic.models;

import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.joml.Vector3f;

import java.util.UUID;

public abstract class RigidBlock {
    private final UUID uniqueId;
    private final BlockDisplay blockDisplay;
    private final Interaction interaction;

    public RigidBlock(BlockDisplay blockDisplay, Interaction interaction) {
        this.blockDisplay = blockDisplay;
        this.interaction = interaction;
        this.uniqueId = UUID.randomUUID();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public BlockDisplay getBlockDisplay() {
        return blockDisplay;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public abstract void applyImpulse(Vector3f direction, Vector3f impulse);
}
