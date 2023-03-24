package lifemoremythicmobs.org.example.lifemoremythicmobs;

import lifemoremythicmobs.org.example.lifemoremythicmobs.register.Register;
import org.bukkit.plugin.java.JavaPlugin;

public final class LifeMoreMythicMobs extends JavaPlugin{

    @Override
    public void onEnable() {

        getLogger().info("はい。。。。。。");

        getServer().getPluginManager().registerEvents(new Register(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("うへぇ。。。。。");
    }

}
