package fr.phylisiumstudio.soraxPhysic.event;

import com.bulletphysics.dynamics.RigidBody;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class RigidblockEvent extends PlayerEvent {
    private final RigidBlock rigidBlock;

    public RigidblockEvent(@NotNull Player who, RigidBlock rigidBlock) {
        super(who);
        this.rigidBlock = rigidBlock;
    }

    public RigidBlock getRigidBlock() {
        return rigidBlock;
    }
}
