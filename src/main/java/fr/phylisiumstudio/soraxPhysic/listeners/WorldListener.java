package fr.phylisiumstudio.soraxPhysic.listeners;

import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {
    private final PhysicsManager physicsManager;

    public WorldListener(PhysicsManager physicsManager) {
        this.physicsManager = physicsManager;
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        physicsManager.unregisterWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        physicsManager.registerWorld(event.getWorld());
    }
}
