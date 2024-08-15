package fr.phylisiumstudio.soraxPhysic.listeners;

import com.bulletphysics.dynamics.RigidBody;
import fr.phylisiumstudio.logic.ItemLinker;
import fr.phylisiumstudio.soraxPhysic.ItemLinkerManager;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.consumers.ToolHeld;
import fr.phylisiumstudio.soraxPhysic.consumers.ToolUnheld;
import fr.phylisiumstudio.soraxPhysic.event.RightClickRigidblockEvent;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

public class ToolsListener implements Listener {
    private final PhysicsManager physicsManager;
    private final ItemLinkerManager itemLinkerManager;

    private final Map<Integer, ToolHeld> toolHeldMap;
    private final Map<Integer, ToolUnheld> toolUnheldMap;

    public ToolsListener(PhysicsManager physicsManager, ItemLinkerManager itemLinkerManager) {
        this.physicsManager = physicsManager;
        this.itemLinkerManager = itemLinkerManager;

        this.toolHeldMap = new HashMap<>();
        this.toolUnheldMap = new HashMap<>();

    }

    @EventHandler
    public void onToolHeld(PlayerInventorySlotChangeEvent event){
        ItemStack newItem = event.getNewItemStack();
        ItemStack oldItem = event.getOldItemStack();

        Integer newUUID = newItem.hashCode();
        Integer oldUUID = oldItem.hashCode();

        if (toolHeldMap.containsKey(newUUID)){
            toolHeldMap.get(newUUID).onToolHeld(newItem, event);
        }

        if (toolUnheldMap.containsKey(oldUUID)){
            toolUnheldMap.get(oldUUID).onToolUnheld(oldItem, event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRightClickRigidbody(RightClickRigidblockEvent event){
        Vector clickedPosition = event.getClickLocation().toVector();
        RigidBlock rigidBlock = event.getRigidBlock();
        Player player = event.getPlayer();

        player.spawnParticle(Particle.CRIT, clickedPosition.getX(), clickedPosition.getY(), clickedPosition.getZ(), 100);

        Location playerLocation = event.getPlayer().getEyeLocation();
        Vector playerDirection = playerLocation.getDirection();
        playerDirection.normalize();
        Vector direction = playerDirection.subtract(clickedPosition);
        Vector3f impulse = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());

        impulse.scale(5);

        org.joml.Vector3f impulseJoml = new org.joml.Vector3f(impulse.x, impulse.y, impulse.z);
        org.joml.Vector3f clickedPositionJoml = new org.joml.Vector3f((float) clickedPosition.getX(), (float) clickedPosition.getY(), (float) clickedPosition.getZ());
        rigidBlock.applyImpulse(clickedPositionJoml, impulseJoml);
    }

    public void registerToolHeld(Integer uuid, ToolHeld toolHeld){
        toolHeldMap.put(uuid, toolHeld);
    }

    public void registerToolUnheld(Integer uuid, ToolUnheld toolUnheld){
        toolUnheldMap.put(uuid, toolUnheld);
    }
}
