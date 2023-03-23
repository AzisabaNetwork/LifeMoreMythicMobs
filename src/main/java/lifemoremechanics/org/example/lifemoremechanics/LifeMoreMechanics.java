package lifemoremechanics.org.example.lifemoremechanics;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicConditionLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import lifemoremechanics.org.example.lifemoremechanics.Condition.RealTimeConditions;
import lifemoremechanics.org.example.lifemoremechanics.Mechanic.ParticleVerticalRingMechanic;
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

        String mechanic = e.getMechanicName();

        if( mechanic.equalsIgnoreCase("takeinv") ) {
            e.register(new TakeItemMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("particleverticalring") || mechanic.equalsIgnoreCase("pvr") || mechanic.equalsIgnoreCase("pvring") ) {
            e.register(new ParticleVerticalRingMechanic(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicConditionLoad(MythicConditionLoadEvent e) {

        if ( e.getConditionName().equalsIgnoreCase("realtime") ) {
            e.register(new RealTimeConditions(e.getConfig()));
        }

    }

}
