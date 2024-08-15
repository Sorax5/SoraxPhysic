package fr.phylisiumstudio.soraxPhysic.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.bulletphysics.dynamics.RigidBody;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import fr.phylisiumstudio.soraxPhysic.ItemLinkerManager;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandAlias("physics")
@Description("Physics related commands")
public class PhysicsCommands extends BaseCommand {
    @Dependency
    private PhysicsManager physicsManager;

    @Dependency
    private ItemLinkerManager itemLinkerManager;

    @Dependency
    private SessionManager sessionManager;

    @Subcommand("create")
    @Description("Create a shape")
    private class CreateCommands extends BaseCommand {
        @Subcommand("box")
        @CommandCompletion("@blocks @range:0.1-100 @range:0.1-10 @range:0.1-10 @range:0.1-10")
        @Description("Create a box shape")
        @CommandPermission("physics.create.box")
        public void createBox(Player sender, Material block, float mass, float xscale, float yscale, float zscale){
            physicsManager.createBoxShape(sender.getEyeLocation(), block, mass, xscale, yscale, zscale);
            sender.sendMessage("Box created");
        }

        @Subcommand("sphere")
        @CommandCompletion("@blocks @range:0.1-100 @range:0.1-10")
        @Description("Create a sphere shape")
        @CommandPermission("physics.create.sphere")
        public void createSphere(Player sender, Material block, float mass, float radius){
            physicsManager.createsphereShape(sender.getEyeLocation(), block, mass, radius);
            sender.sendMessage("Sphere created");
        }

        @Subcommand("convert")
        @Description("Convert the WorldEdit selection to physics shapes (beta feature do not use if you don't know what you are doing)")
        @CommandPermission("physics.create.convert")
        public void convertSelection(Player player, float impulse, float damping, float tau){
            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
            LocalSession session = sessionManager.get(wePlayer);

            Region region;
            World world = session.getSelectionWorld();
            org.bukkit.World bukkitWorld = BukkitAdapter.adapt(world);

            try {
                if (world == null) {
                    throw new IncompleteRegionException();
                }
                region = session.getSelection(world);
            }
            catch (IncompleteRegionException e) {
                throw new IllegalArgumentException("Please make a WorldEdit selection first");
            }

            Map<Block,RigidBody> bodies = new HashMap<>();

            // get block in the region
            for (BlockVector3 blockVector3 : region) {
                Block block = bukkitWorld.getBlockAt(blockVector3.x(), blockVector3.y(), blockVector3.z());

                if(block.getType().isAir()){
                    continue;
                }

                RigidBody body = physicsManager.createBoxShape(block.getLocation(), block.getType(), 1, 1, 1, 1);
                bodies.put(block, body);
            }

            bodies.forEach((block, body) -> {
                List<Block> blocks = new ArrayList<>();
                blocks.add(block.getRelative(1, 0, 0));
                blocks.add(block.getRelative(-1, 0, 0));
                blocks.add(block.getRelative(0, 1, 0));
                blocks.add(block.getRelative(0, -1, 0));

                for (Block block1 : blocks) {
                    // if block is in bodies
                    if(bodies.containsKey(block1)){
                        BlockFace blockFace = block.getFace(block1);
                        assert blockFace != null;
                        BlockFace oppositeFace = blockFace.getOppositeFace();
                        assert oppositeFace != null;

                        physicsManager.linkRigidBody(body, bodies.get(block1), blockFace, oppositeFace);
                    }
                }
            });

            try(EditSession editSession = session.createEditSession(wePlayer)){
                BlockState blockState = BukkitAdapter.adapt(Material.AIR.createBlockData());
                editSession.setBlocks(region, blockState);
            } catch (MaxChangedBlocksException e) {
                throw new IllegalArgumentException("Too many blocks to change");
            }

            player.sendMessage("Selection converted");
        }
    }

    @Subcommand("tools")
    @Description("Don't work for the moment")
    private class ToolsCommands extends BaseCommand {
        @Subcommand("linker")
        public void createLinker(Player sender){
            ItemStack linker = itemLinkerManager.CreateLinker(sender.getUniqueId());
            sender.getInventory().addItem(linker);
        }
    }

    @Subcommand("clear")
    @Description("Clear all shapes in the physics engine")
    @CommandPermission("physics.clear")
    public void clearShapes(Player sender){
        physicsManager.clearAll();
        sender.sendMessage("Shapes cleared");
    }

    @Subcommand("freeze")
    @Description("Freeze the time in the physics engine")
    @CommandPermission("physics.freeze")
    public void freezeTime(Player sender){
        physicsManager.setTimeFreeze(!physicsManager.isTimeFreeze());
    }

    @Subcommand("timespan")
    @CommandCompletion("@range:0.1-1")
    @Description("Set the timespan for the physics engine (do not touch if you don't know what you are doing)")
    @CommandPermission("physics.timespan")
    public void setTimeSpan(Player sender, float timespan){
        physicsManager.setTimespan(timespan);
        sender.sendMessage("Timespan set to " + timespan);
    }

    @Subcommand("substeps")
    @CommandCompletion("@range:1-60")
    @Description("Set the number of substeps for the physics engine (do not touch if you don't know what you are doing)")
    @CommandPermission("physics.substeps")
    public void setMaxSubSteps(Player sender, int maxSubSteps){
        physicsManager.setMaxSubSteps(maxSubSteps);
        sender.sendMessage("Max substeps set to " + maxSubSteps);
    }

    @Subcommand("chunk")
    @Description("Convert the chunk where the player is standing to a physics chunk")
    @CommandPermission("physics.chunk")
    public void convertChunk(Player sender){
        physicsManager.ConvertChunk(sender.getLocation().getChunk());
        sender.sendMessage("Chunk converted");
    }

    @HelpCommand
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}
