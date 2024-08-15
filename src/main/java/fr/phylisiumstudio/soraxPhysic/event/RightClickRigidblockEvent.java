package fr.phylisiumstudio.soraxPhysic.event;

import com.bulletphysics.dynamics.RigidBody;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RightClickRigidblockEvent extends RigidblockEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Location clickLocation;

    public RightClickRigidblockEvent(@NotNull Player who, RigidBlock rigidBlock, Location clickLocation) {
        super(who, rigidBlock);
        this.clickLocation = clickLocation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Location getClickLocation() {
        return clickLocation;
    }
}
