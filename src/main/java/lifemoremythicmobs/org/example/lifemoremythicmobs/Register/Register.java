package lifemoremythicmobs.org.example.lifemoremythicmobs.Register;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicConditionLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import lifemoremythicmobs.org.example.lifemoremythicmobs.Condition.RealTimeConditions;
import lifemoremythicmobs.org.example.lifemoremythicmobs.Mechanic.*;
import lifemoremythicmobs.org.example.lifemoremythicmobs.Placeholder.MMIDPlaceholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Register implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent e)	{

        String mechanic = e.getMechanicName();

        if ( mechanic.equalsIgnoreCase("takeinv") ) {
            e.register(new TakeItemMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("particleverticalring") || mechanic.equalsIgnoreCase("pvr") || mechanic.equalsIgnoreCase("pvring") ) {
            e.register(new ParticleVerticalRingMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("bossbar") ) {
            e.register(new BossBarMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("removebossbar") || mechanic.equalsIgnoreCase("bossbarremove") ) {
            e.register(new RemoveBossBarMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("modifybossbar") || mechanic.equalsIgnoreCase("bossbarmodify")) {
            e.register(new ModifyBossBarMechanic(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicConditionLoad(MythicConditionLoadEvent e) {

        String condition = e.getConditionName();

        if ( condition.equalsIgnoreCase("realtime") ) {
            e.register(new RealTimeConditions(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicPlaceholderLoad(MythicReloadedEvent e) {

        MMIDPlaceholder.register();

    }

}
