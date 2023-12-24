package net.azisaba.lifemoremythicmobs;

import net.azisaba.lifemoremythicmobs.listener.Register;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LifeMoreMythicMobs extends JavaPlugin{

    @Override
    public void onEnable() {

        getLogger().info("はい。。。。。。");

        getServer().getPluginManager().registerEvents(new Register(), this);
        Bukkit.getScheduler().runTask(this, Register::reloadPlaceholders);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("うへぇ。。。。。");
    }

}
