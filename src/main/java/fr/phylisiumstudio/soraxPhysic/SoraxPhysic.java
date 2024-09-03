package fr.phylisiumstudio.soraxPhysic;

import co.aikar.commands.PaperCommandManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.SessionManager;
import fr.phylisiumstudio.soraxPhysic.commands.PhysicsCommands;
import fr.phylisiumstudio.soraxPhysic.listeners.RigidbodyListener;
import fr.phylisiumstudio.soraxPhysic.listeners.PlayerActionListener;
import fr.phylisiumstudio.soraxPhysic.listeners.WorldListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public final class SoraxPhysic extends JavaPlugin {

    private static SoraxPhysic instance;

    private PaperCommandManager commandManager;
    private PhysicsManager physicsManager;

    @Override
    public void onEnable() {
        instance = this;

        setupPhysics();
        setupCommands();
        setupListeners();
        setupBstats();
    }

    @Override
    public void onDisable() {
        this.physicsManager.clear();
        this.physicsManager.stop();
    }

    private void setupBstats(){
        int pluginId = 23021;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("used_worldedit_version", WorldEdit::getVersion));
        metrics.addCustomChart(new SimplePie("used_physics_version", () -> this.getPluginMeta().getVersion()));
    }

    private void setupCommands(){
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.registerDependency(PhysicsManager.class, physicsManager);
        commandManager.registerDependency(SessionManager.class, WorldEdit.getInstance().getSessionManager());

        commandManager.registerCommand(new PhysicsCommands());

        commandManager.getCommandCompletions().registerAsyncCompletion("blocks", c -> {
            return List.of(Arrays.stream(Material.values()).filter(Material::isBlock).map(m -> m.name().toLowerCase()).toArray(String[]::new));
        });
        commandManager.getCommandContexts().registerContext(Material.class, c -> {
            Material material;

            for (String arg : c.getArgs()) {
                try {
                    material = Material.valueOf(arg.toUpperCase());
                    return material;
                } catch (IllegalArgumentException ignored) {
                }
            }

            throw new IllegalArgumentException("Invalid material");
        });
    }

    private void setupPhysics() {
        physicsManager = new PhysicsManager();
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new WorldListener(physicsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerActionListener(physicsManager), this);
        getServer().getPluginManager().registerEvents(new RigidbodyListener(physicsManager, getServer()), this);
    }

    public static SoraxPhysic getInstance() {
        return instance;
    }

    /**
     * Get the physics manager
     * @return the physics manager
     */
    public PhysicsManager getPhysicsManager() {
        return physicsManager;
    }
}
