package fr.phylisiumstudio.soraxPhysic.listeners;

import com.bulletphysics.dynamics.RigidBody;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.event.RightClickRigidblockEvent;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.List;

public class RigidbodyListener implements Listener {
    private PhysicsManager physicsManager;
    private Server server;

    public RigidbodyListener(PhysicsManager physicsManager, Server server) {
        this.physicsManager = physicsManager;
        this.server = server;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event){
        if(!(event.getRightClicked() instanceof Interaction hitbox)){
            return;
        }

        List<RigidBlock> rigidBlocks = physicsManager.getRigidBlocks();
        World bukkitWorld = event.getPlayer().getWorld();

        RigidBlock rigidBlock = rigidBlocks.stream()
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
}