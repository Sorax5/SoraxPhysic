package fr.phylisiumstudio.soraxPhysic.listeners;

import com.bulletphysics.dynamics.RigidBody;
import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.event.LeftClickRigidblockEvent;
import fr.phylisiumstudio.soraxPhysic.event.RightClickRigidblockEvent;
import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.List;

public class RigidbodyListener implements Listener {
    private final PhysicsManager physicsManager;
    private final Server server;

    public RigidbodyListener(PhysicsManager physicsManager, Server server) {
        this.physicsManager = physicsManager;
        this.server = server;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event){
        if(!(event.getRightClicked() instanceof Interaction hitbox)){
            return;
        }

        WorldPhysics world = this.physicsManager.getWorldPhysics(event.getPlayer().getWorld().getUID());

        List<IRigidBlock> rigidBlocks = world.getBlocks();
        World bukkitWorld = event.getPlayer().getWorld();

        IRigidBlock rigidBlock = rigidBlocks.stream()
                .filter(motionState -> motionState.getInteraction().equals(hitbox))
                .findFirst()
                .orElse(null);

        assert rigidBlock != null;
        BlockDisplay blockDisplay = rigidBlock.getBlockDisplay();
        RigidBody body = rigidBlock.getRigidBody();

        if (blockDisplay == null || body == null) {
            return;
        }
        Location clickedLocation = event.getClickedPosition().toLocation(bukkitWorld);

        RightClickRigidblockEvent rightClickRigidbodyEvent = new RightClickRigidblockEvent(event.getPlayer(), rigidBlock, clickedLocation);
        this.server.getPluginManager().callEvent(rightClickRigidbodyEvent);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Interaction hitbox)){
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        WorldPhysics worldPhysics = this.physicsManager.getWorldPhysics(player.getWorld().getUID());

        List<IRigidBlock> rigidBlocks = worldPhysics.getBlocks();

        IRigidBlock rigidBlock = rigidBlocks.stream()
                .filter(motionState -> motionState.getInteraction().equals(hitbox))
                .findFirst()
                .orElse(null);

        assert rigidBlock != null;
        BlockDisplay blockDisplay = rigidBlock.getBlockDisplay();
        RigidBody body = rigidBlock.getRigidBody();

        if (blockDisplay == null || body == null) {
            return;
        }
        Location clickedLocation = event.getEntity().getLocation();

        LeftClickRigidblockEvent rightClickRigidbodyEvent = new LeftClickRigidblockEvent(player, rigidBlock, clickedLocation);
        this.server.getPluginManager().callEvent(rightClickRigidbodyEvent);
    }
}
