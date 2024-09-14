package fr.phylisiumstudio.soraxPhysic.event;

import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LeftClickRigidblockEvent extends RigidblockEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Location clickLocation;

    public LeftClickRigidblockEvent(@NotNull Player who, IRigidBlock rigidBlock, Location clickLocation) {
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
