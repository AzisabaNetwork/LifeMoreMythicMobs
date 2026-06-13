package net.azisaba.lifemoremythicmobs;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.azisaba.lifemoremythicmobs.commands.RootCommand;
import net.azisaba.lifemoremythicmobs.listener.BowForceListener;
import net.azisaba.lifemoremythicmobs.listener.JoinListener;
import net.azisaba.lifemoremythicmobs.listener.Register;
import net.azisaba.lifemoremythicmobs.mechanic.ModifyPlayerAttributeMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LifeMoreMythicMobs extends JavaPlugin{
    private static LifeMoreMythicMobs instance;
    public String server = "";

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("LifeMoreMythicMobs has been enabled.");

        if (!Objects.requireNonNull(getConfig().getString("server-override", "")).isEmpty()) {
            server = getConfig().getString("server-override", "");
        }
        Objects.requireNonNull(getCommand("lmmm")).setExecutor(new RootCommand(this));
        getServer().getPluginManager().registerEvents(new Register(), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.SpawnerToolListener(this), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.SpawnerManagerListener(this), this);
        getServer().getPluginManager().registerEvents(new net.azisaba.lifemoremythicmobs.listener.UpgradeListener(this), this);
        getServer().getPluginManager().registerEvents(new BowForceListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getScheduler().runTask(this, Register::reloadPlaceholders);

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
}
