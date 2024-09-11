package fr.phylisiumstudio.soraxPhysic.listeners;

import fr.phylisiumstudio.bullet.BulletWorldPhysics;
import fr.phylisiumstudio.logic.WorldPhysics;
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
        WorldPhysics worldPhysics = physicsManager.getWorldPhysics(event.getWorld().getUID());
        physicsManager.unregisterWorld(worldPhysics);
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        BulletWorldPhysics worldPhysics = new BulletWorldPhysics(event.getWorld());
        physicsManager.registerWorld(worldPhysics);
    }
}
