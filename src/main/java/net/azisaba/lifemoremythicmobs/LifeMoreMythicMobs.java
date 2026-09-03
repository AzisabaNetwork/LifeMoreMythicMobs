package net.azisaba.lifemoremythicmobs;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lifemoremythicmobs.commands.RootCommand;
import net.azisaba.lifemoremythicmobs.commands.ItemAttrCommand;
import net.azisaba.lifemoremythicmobs.config.IgaConfigService;
import net.azisaba.lifemoremythicmobs.gui.AttrGuiManager;
import net.azisaba.lifemoremythicmobs.listener.BowForceListener;
import net.azisaba.lifemoremythicmobs.listener.JoinListener;
import net.azisaba.lifemoremythicmobs.listener.Register;
import net.azisaba.lifemoremythicmobs.listener.DailyScoreResetter;
import net.azisaba.lifemoremythicmobs.listener.EquipLockListener;
import net.azisaba.lifemoremythicmobs.listener.KillMessageDamageListener;
import net.azisaba.lifemoremythicmobs.listener.WorldChangeRemovePotionEffectListener;
import net.azisaba.lifemoremythicmobs.mechanic.ModifyPlayerAttributeMechanic;
import net.azisaba.lifemoremythicmobs.util.ArmorGuard.ArmorAttributeGuard;
import net.azisaba.lifemoremythicmobs.util.ArmorGuard.ArmorGuardSettings;
import net.azisaba.lifemoremythicmobs.util.CharReorderGui.CharReorderGuiListener;
import net.azisaba.lifemoremythicmobs.util.DBConnector;
import net.azisaba.lifemoremythicmobs.util.TickCounter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.PluginCommand;
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
        // Safely obtain and register the root command; avoid crashing on missing command definition
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
        getServer().getPluginManager().registerEvents(new Register(), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.SpawnerToolListener(this), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.SpawnerManagerListener(this), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.UpgradeListener(this), this);
        getServer().getPluginManager().registerEvents(new BowForceListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(new WorldChangeRemovePotionEffectListener(this), this);
        getServer().getPluginManager().registerEvents(new KillMessageDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new CharReorderGuiListener(), this);
        getServer().getPluginManager().registerEvents(new EquipLockListener(), this);
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
            fetchServer(players.get(0));
        }
    }

    @Override
    public void onDisable() {
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
