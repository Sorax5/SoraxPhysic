package fr.phylisiumstudio.soraxPhysic.commands;

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
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import fr.phylisiumstudio.logic.WorldPhysics;
import fr.phylisiumstudio.soraxPhysic.PhysicsManager;
import fr.phylisiumstudio.soraxPhysic.models.RigidBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PhysicsCommands {

    private final PhysicsManager physicsManager;
    private final SessionManager sessionManager;

    public PhysicsCommands(PhysicsManager physicsManager, SessionManager sessionManager) {
        this.physicsManager = physicsManager;
        this.sessionManager = sessionManager;
        registerCommands();
    }

    private void registerCommands() {
        new CommandAPICommand("physics")
                .withSubcommand(new CommandAPICommand("create")
                        .withSubcommand(new CommandAPICommand("box")
                                .withArguments(customMaterialArgument("block"), new FloatArgument("mass"), new FloatArgument("xscale"), new FloatArgument("yscale"), new FloatArgument("zscale"))
                                .executesPlayer((sender, args) -> {
                                    Material block = (Material) args.get("block");
                                    float mass = (float) args.get("mass");
                                    float xscale = (float) args.get("xscale");
                                    float yscale = (float) args.get("yscale");
                                    float zscale = (float) args.get("zscale");
                                    physicsManager.createBox(sender.getEyeLocation(), block.createBlockData(), mass, xscale, yscale, zscale);
                                    sender.sendMessage("Box created");
                                })
                        )
                        .withSubcommand(new CommandAPICommand("sphere")
                                .withArguments(customMaterialArgument("block"), new FloatArgument("mass"), new FloatArgument("radius"))
                                .executesPlayer((sender, args) -> {
                                    Material block = (Material) args.get("block");
                                    float mass = (float) args.get("mass");
                                    float radius = (float) args.get("radius");
                                    physicsManager.createSphere(sender.getEyeLocation(), block.createBlockData(), mass, radius);
                                    sender.sendMessage("Sphere created");
                                })
                        )
                        .withSubcommand(new CommandAPICommand("convert")
                                .withArguments(new FloatArgument("impulse"), new FloatArgument("damping"), new FloatArgument("tau"))
                                .executesPlayer((player, args) -> {
                                    float impulse = (float) args.get("impulse");
                                    float damping = (float) args.get("damping");
                                    float tau = (float) args.get("tau");
                                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                                    LocalSession session = sessionManager.get(wePlayer);

                                    Region region;
                                    World world = session.getSelectionWorld();
                                    org.bukkit.World bukkitWorld = BukkitAdapter.adapt(world);
                                    WorldPhysics worldPhysics = physicsManager.getWorldPhysics(bukkitWorld.getUID());

                                    try {
                                        if (world == null) {
                                            throw new IncompleteRegionException();
                                        }
                                        region = session.getSelection(world);
                                    } catch (IncompleteRegionException e) {
                                        throw new IllegalArgumentException("Please make a WorldEdit selection first");
                                    }

                                    Map<Block, RigidBlock> bodies = new HashMap<>();

                                    // get block in the region
                                    for (BlockVector3 blockVector3 : region) {
                                        Block block = bukkitWorld.getBlockAt(blockVector3.x(), blockVector3.y(), blockVector3.z());

                                        if (block.getType().isAir()) {
                                            continue;
                                        }

                                        RigidBlock body = worldPhysics.createBox(block.getLocation(), block.getBlockData(), 1, 1, 1, 1);
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
                                            if (bodies.containsKey(block1)) {
                                                BlockFace blockFace = block.getFace(block1);
                                                assert blockFace != null;
                                                BlockFace oppositeFace = blockFace.getOppositeFace();
                                                assert oppositeFace != null;

                                                worldPhysics.linkRigidBlock(bodies.get(block), bodies.get(block1));
                                            }
                                        }
                                    });

                                    try (EditSession editSession = session.createEditSession(wePlayer)) {
                                        BlockState blockState = BukkitAdapter.adapt(Material.AIR.createBlockData());
                                        editSession.setBlocks(region, blockState);
                                    } catch (MaxChangedBlocksException e) {
                                        throw new IllegalArgumentException("Too many blocks to change");
                                    }

                                    player.sendMessage("Selection converted");
                                })
                        )
                )
                .withSubcommand(new CommandAPICommand("clear")
                        .executesPlayer((sender, args) -> {
                            WorldPhysics worldPhysics = physicsManager.getWorldPhysics(sender.getWorld().getUID());
                            if (worldPhysics == null) {
                                throw new IllegalArgumentException("The world is not managed by the physics engine");
                            }
                            worldPhysics.clear();
                            sender.sendMessage("Shapes cleared for the world " + sender.getWorld().getName());
                        })
                )
                .withSubcommand(new CommandAPICommand("freeze")
                        .executesPlayer((sender, args) -> {
                            WorldPhysics worldPhysics = physicsManager.getWorldPhysics(sender.getWorld().getUID());
                            if (worldPhysics == null) {
                                throw new IllegalArgumentException("The world is not managed by the physics engine");
                            }
                            boolean freeze = !worldPhysics.isFrozen();
                            worldPhysics.setFreeze(freeze);
                            sender.sendMessage("Time " + (freeze ? "frozen" : "unfrozen") + " for the world " + sender.getWorld().getName());
                        })
                )
                .withSubcommand(new CommandAPICommand("timespan")
                        .withArguments(new FloatArgument("timespan"))
                        .executesPlayer((sender, args) -> {
                            float timespan = (float) args.get("timespan");
                            WorldPhysics worldPhysics = physicsManager.getWorldPhysics(sender.getWorld().getUID());
                            if (worldPhysics == null) {
                                throw new IllegalArgumentException("The world is not managed by the physics engine");
                            }
                            worldPhysics.setTimespan(timespan);
                            sender.sendMessage("Timespan set to " + timespan + "s for the world " + sender.getWorld().getName());
                        })
                )
                .withSubcommand(new CommandAPICommand("substeps")
                        .withArguments(new IntegerArgument("maxSubSteps"))
                        .executesPlayer((sender, args) -> {
                            int maxSubSteps = (int) args.get("maxSubSteps");
                            WorldPhysics worldPhysics = physicsManager.getWorldPhysics(sender.getWorld().getUID());
                            if (worldPhysics == null) {
                                throw new IllegalArgumentException("The world is not managed by the physics engine");
                            }
                            worldPhysics.setMaxSubSteps(maxSubSteps);
                            sender.sendMessage("Max substeps set to " + maxSubSteps + " for the world " + sender.getWorld().getName());
                        })
                )
                .withSubcommand(new CommandAPICommand("chunk")
                        .executesPlayer((sender, args) -> {
                            WorldPhysics worldPhysics = physicsManager.getWorldPhysics(sender.getWorld().getUID());
                            if (worldPhysics == null) {
                                throw new IllegalArgumentException("The world is not managed by the physics engine");
                            }
                            Vector3f pos1 = new Vector3f(sender.getLocation().getBlockX(), sender.getLocation().getBlockY(), sender.getLocation().getBlockZ());
                            Vector3f pos2 = new Vector3f(sender.getLocation().getBlockX() + 16, sender.getLocation().getBlockY() + 16, sender.getLocation().getBlockZ() + 16);
                            worldPhysics.convertChunk(pos1, pos2);
                            sender.sendMessage("Chunk converted");
                        })
                )
                .register();
    }

    private Argument<Material> customMaterialArgument(String nodename) {
        return new CustomArgument<Material, String>(new StringArgument(nodename), (info) ->{
            Material material = Material.getMaterial(info.input());

            if (material == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(new CustomArgument.MessageBuilder("Invalid material").appendArgInput());
            }

            return material;
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->{
            return Stream.of(Material.values()).map(Material::name).toArray(String[]::new);
        }));
    }
}
