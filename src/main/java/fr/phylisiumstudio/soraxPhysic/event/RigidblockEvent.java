package fr.phylisiumstudio.soraxPhysic.event;

import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class RigidblockEvent extends PlayerEvent {
    private final IRigidBlock rigidBlock;

    public RigidblockEvent(@NotNull Player who, IRigidBlock rigidBlock) {
        super(who);
        this.rigidBlock = rigidBlock;
    }

    public IRigidBlock getRigidBlock() {
        return rigidBlock;
    }
}
