package net.azisaba.lifemoremythicmobs;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lifemoremythicmobs.commands.ItemAttrCommand;
import net.azisaba.lifemoremythicmobs.commands.RootCommand;
import net.azisaba.lifemoremythicmobs.config.IgaConfigService;
import net.azisaba.lifemoremythicmobs.gui.AttrGuiManager;
import net.azisaba.lifemoremythicmobs.listener.*;
import net.azisaba.lifemoremythicmobs.mechanic.ModifyPlayerAttributeMechanic;
import net.azisaba.lifemoremythicmobs.util.ArmorGuard.ArmorAttributeGuard;
import net.azisaba.lifemoremythicmobs.util.ArmorGuard.ArmorGuardSettings;
import net.azisaba.lifemoremythicmobs.util.CharReorderGui.CharReorderGuiListener;
import net.azisaba.lifemoremythicmobs.util.DBConnector;
import net.azisaba.lifemoremythicmobs.util.ManagedEntityRegistry;
import net.azisaba.lifemoremythicmobs.util.MarkStackManager;
import net.azisaba.lifemoremythicmobs.util.ParryWindowManager;
import net.azisaba.lifemoremythicmobs.util.ResourceNodeManager;
import net.azisaba.lifemoremythicmobs.util.SkillCheckManager;
import net.azisaba.lifemoremythicmobs.util.TickCounter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LifeMoreMythicMobs extends JavaPlugin{
    private static LifeMoreMythicMobs instance;
    private IgaConfigService configService;
    private AttrGuiManager gui;
    private ArmorAttributeGuard armorGuard;
    public String server = "";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        configService = new IgaConfigService(this);
        configService.reload();
        gui = new AttrGuiManager(this);
        armorGuard = new ArmorAttributeGuard(this, ArmorGuardSettings.fromConfig(getConfig()));
        armorGuard.register();
        getLogger().info("LifeMoreMythicMobs has been enabled.");

        if (!Objects.requireNonNull(getConfig().getString("server-override", "")).isEmpty()) {
            server = getConfig().getString("server-override", "");
        }
        // Safely get and register the root command; avoid crashing on missing command definition
        PluginCommand root = getCommand("lmmm");
        if (root == null) {
            getLogger().severe("Command 'lmmm' is not defined in plugin.yml or failed to load. Disabling plugin to avoid errors.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        root.setExecutor(new RootCommand(this));
        PluginCommand itemAttr = getCommand("itemattr");
        if (itemAttr != null) {
            ItemAttrCommand itemAttrCommand = new ItemAttrCommand();
            itemAttr.setExecutor(itemAttrCommand);
            itemAttr.setTabCompleter(itemAttrCommand);
        }
        if (getConfig().getBoolean("replace-damage-mechanic", true)) {
            net.azisaba.lifemoremythicmobs.util.DamageMechanicOverride.apply(getLogger());
            if (net.azisaba.lifemoremythicmobs.util.DamageMechanicOverride.isActive()) {
                // MythicMobs already built its skills (with its own damage) while it was enabling, before
                // this plugin. Rebuild them once so every damage{...} becomes tDamage from the start.
                Bukkit.getScheduler().runTask(this, () -> {
                    getLogger().info("damage を tDamage として読み直すため、MythicMobs を1回リロードします。");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mythicmobs reload");
                });
            }
        }
        getServer().getPluginManager().registerEvents(new Register(), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.SpawnerToolListener(this), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.SpawnerManagerListener(this), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.UpgradeListener(this), this);
        getServer().getPluginManager().registerEvents(new BowForceListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(new WorldChangeRemovePotionEffectListener(this), this);
        getServer().getPluginManager().registerEvents(new KillMessageDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new LegacyDamageModifiersListener(), this);
        getServer().getPluginManager().registerEvents(new LegacySelfDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new LegacyMobDamageListener(), this);
        getServer().getPluginManager().registerEvents(new CharReorderGuiListener(), this);
        getServer().getPluginManager().registerEvents(new EquipLockListener(), this);
        ManagedEntityRegistry.init(this);
        getServer().getPluginManager().registerEvents(new ManagedEntityRegistry(), this);
        getServer().getPluginManager().registerEvents(new ResourceNodeListener(), this);
        getServer().getPluginManager().registerEvents(new SkillCheckListener(), this);
        getServer().getPluginManager().registerEvents(new ParryWindowListener(), this);
        Bukkit.getScheduler().runTask(this, Register::reloadPlaceholders);
        TickCounter.start();
        // The scoreboard manager is not available until the worlds have finished loading.
        Bukkit.getScheduler().runTask(this, DailyScoreResetter::run);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, message) -> {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subChannel = in.readUTF();
            if (subChannel.equals("GetServer")) {
                String newServer = in.readUTF();
                if (!newServer.equals(server)) {
                    getSLF4JLogger().info("Server name is " + newServer);
                }
                server = newServer;
            }
        });

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (!players.isEmpty()) {
            fetchServer(players.getFirst());
        }
    }

    @Override
    public void onDisable() {
        ResourceNodeManager.clearAll();
        SkillCheckManager.clearAll();
        ParryWindowManager.clearAll();
        MarkStackManager.clearAll();
        ManagedEntityRegistry.shutdownAll();
        ModifyPlayerAttributeMechanic.shutdown();
        if (armorGuard != null) armorGuard.unregister();
        DBConnector.close();
        getLogger().info("LifeMoreMythicMobs has been disabled.");
    }

    public void fetchServer(Player player) {
        if (!server.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public static LifeMoreMythicMobs inst() {
        return instance;
    }

    public IgaConfigService getConfigService() {
        return configService;
    }

    public AttrGuiManager getGui() {
        return gui;
    }
}
