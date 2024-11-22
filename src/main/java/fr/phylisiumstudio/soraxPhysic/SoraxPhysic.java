package fr.phylisiumstudio.soraxPhysic;

import com.sk89q.worldedit.WorldEdit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import fr.phylisiumstudio.soraxPhysic.commands.PhysicsCommands;
import fr.phylisiumstudio.soraxPhysic.listeners.RigidbodyListener;
import fr.phylisiumstudio.soraxPhysic.listeners.PlayerActionListener;
import fr.phylisiumstudio.soraxPhysic.listeners.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoraxPhysic extends JavaPlugin {

    private static SoraxPhysic instance;
    private PhysicsManager physicsManager;

    public SoraxPhysic() {
        instance = this;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true));

        setupPhysics();
        setupCommands();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        instance = this;

        setupListeners();
        setupBstats();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        this.physicsManager.clear();
        this.physicsManager.stop();
    }

    private void setupBstats(){
        int pluginId = 23021;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("used_worldedit_version", WorldEdit::getVersion));
        metrics.addCustomChart(new Metrics.SimplePie("used_physics_version", () -> this.getPluginMeta().getVersion()));
    }

    private void setupCommands(){
        PhysicsCommands physicsCommands = new PhysicsCommands(physicsManager, WorldEdit.getInstance().getSessionManager());
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
