package fr.phylisiumstudio.soraxPhysic.listeners;

import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.event.RightClickRigidblockEvent;
import fr.phylisiumstudio.logic.IRigidBlock;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import javax.vecmath.Vector3f;

public class PlayerActionListener implements Listener {
    private final PhysicsManager physicsManager;

    public PlayerActionListener(PhysicsManager physicsManager) {
        this.physicsManager = physicsManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRightClickRigidbody(RightClickRigidblockEvent event){
        Vector clickedPosition = event.getClickLocation().toVector();
        IRigidBlock rigidBlock = event.getRigidBlock();
        Player player = event.getPlayer();

        player.spawnParticle(Particle.CRIT, clickedPosition.getX(), clickedPosition.getY(), clickedPosition.getZ(), 100);

        Location playerLocation = event.getPlayer().getEyeLocation();
        Vector playerDirection = playerLocation.getDirection();
        playerDirection.normalize();
        Vector direction = playerLocation.getDirection().subtract(clickedPosition);
        Vector3f impulse = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());

        impulse.scale(5f);

        org.joml.Vector3f impulseJoml = new org.joml.Vector3f(impulse.x, impulse.y, impulse.z);
        org.joml.Vector3f clickedPositionJoml = new org.joml.Vector3f((float) clickedPosition.getX(), (float) clickedPosition.getY(), (float) clickedPosition.getZ());
        rigidBlock.applyImpulse(clickedPositionJoml, impulseJoml);
    }

    /*@EventHandler
    public void onLeftClickRigidbody(LeftClickRigidblockEvent event){
        Vector clickedPosition = event.getClickLocation().toVector();
        RigidBlock rigidBlock = event.getRigidBlock();
        Player player = event.getPlayer();

        player.spawnParticle(Particle.CRIT, clickedPosition.getX(), clickedPosition.getY(), clickedPosition.getZ(), 100);

        Location playerLocation = event.getPlayer().getEyeLocation();
        Vector playerDirection = playerLocation.getDirection();
        playerDirection.normalize();
        Vector direction = playerDirection.subtract(clickedPosition);
        Vector3f impulse = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());

        impulse.scale(0.1f);

        org.joml.Vector3f impulseJoml = new org.joml.Vector3f(impulse.x, impulse.y, impulse.z);
        org.joml.Vector3f clickedPositionJoml = new org.joml.Vector3f((float) clickedPosition.getX(), (float) clickedPosition.getY(), (float) clickedPosition.getZ());
        rigidBlock.applyImpulse(clickedPositionJoml, impulseJoml);
    }*/
}
