package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final LifeMoreMythicMobs plugin;

    public JoinListener(LifeMoreMythicMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.fetchServer(e.getPlayer());
    }
}
