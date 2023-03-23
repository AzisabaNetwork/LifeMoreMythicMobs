package lifemoremechanics.org.example.lifemoremechanics;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import lifemoremechanics.org.example.lifemoremechanics.Mechanic.TakeItemMechanic;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class LifeMoreMechanics extends JavaPlugin implements Listener {

    private Logger log;

    @Override
    public void onEnable() {
        log = this.getLogger();
        Bukkit.getPluginManager().registerEvents(this, this);

        log.info("はい。。。。。。");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info("うへぇ。。。。。");
    }

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent e)	{

        log.info(e.getMechanicName());

        if( e.getMechanicName().equalsIgnoreCase("takeinv") )	{
            Bukkit.broadcastMessage("takeitem getConfig");
            e.register(new TakeItemMechanic(e.getConfig()));
        }

    }






}
